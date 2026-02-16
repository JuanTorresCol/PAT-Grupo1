package edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class ReservaController {

    //horario del sistema (08:00–22:00)
    private static final LocalTime APERTURA = LocalTime.of(8, 0);
    private static final LocalTime CIERRE = LocalTime.of(22, 0);
    private static final Logger log = LoggerFactory.getLogger(ReservaController.class);

    //seria el record disponibilidad
    private record SlotInfo(LocalDate date, int slotStart, int slotEnd) {}

    private final ServicioPistas servicio;
    @Autowired
    public ReservaController(ServicioPistas servicio) { this.servicio = servicio; }

    //almacenamiento de las reservas
    private final Map<String, Reserva> reservas = new ConcurrentHashMap<>();


    //crear reserva
    //respuesta: 201, 400, 401, 404, 409
    @PostMapping("/pistaPadel/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    public Reserva crear(@Valid @RequestBody ReservaCreateRequest req, BindingResult br) {

        log.info("Solicitud de creación de reserva recibida");

        // 400 campos inválidos
        if (br.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campos inválidos");
        }

        log.debug("Comprobando existencia de la pista");
        //hay que comprobar que la pista exista y que esté activa (404 y 400)
        log.warn("Intento de reserva sobre pista inexistente.");
        Pista pista = comprobarPistaExiste(req.courtId());
        if (Boolean.FALSE.equals(pista.activa())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La pista no está activa");
        }

        //validación de fecha, hora y cálculo de franjas
        SlotInfo s = validarYCalcularSlots(req.date(), req.startTime(), req.durationMins());
        LocalDate date = s.date();
        int slotStart = s.slotStart();
        int slotEnd = s.slotEnd();


        log.warn("Intento de reserva con solapamiento.");
        //409 comprobación de slots libres
        disponibilidadSlots(req.courtId(), date, slotStart, slotEnd);
        //marcar slots como ocupados
        setSlots(req.courtId(), date, slotStart, slotEnd, true);


        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        //201 reserva creada
        Reserva nueva_reserva = new Reserva(
                UUID.randomUUID().toString(), //código id de 36 caracteres
                username,
                req.courtId(),
                date,
                slotStart,
                slotEnd,
                ReservaStatus.CONFIRMADA,
                Instant.now()
        );
        reservas.put(nueva_reserva.id(), nueva_reserva);
        log.info("Reserva creada correctamente.");
        return nueva_reserva;
    }

    //get todas las reservas de una persona
    //los filtros from/to son opcionales en el enunciado y no se implementan aquí
    // 200, 401
    @GetMapping("/pistaPadel/reservations")
    public List<Reserva> listar() {
        log.debug("Solicitud para listar todas las reservas");

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        List<Reserva> resultado = new ArrayList<>();
        for (Reserva r : reservas.values()) {
            if (!r.username().equals(username)) {continue;}

            if (r.estado() == ReservaStatus.CONFIRMADA && reservaPasada(r)) {
                r = new Reserva(r.id(), r.username(), r.courtId(), r.date(),
                        r.slotStart(), r.slotEnd(), ReservaStatus.PASADA, r.createdAt());
            }
            resultado.add(r);
        }
        log.info("Se han devuelto {} reservas", resultado.size());
        return resultado; //200 ok
    }

    //get reservas por id de reserva
    // 200, 401, 403, 404
    @GetMapping("/pistaPadel/reservations/{reservaId}")
    public Reserva buscar(@PathVariable String reservaId) {

        log.debug("Solicitud para obtener reserva con ID {}", reservaId);
        Reserva r = obtenerReserva(reservaId);
        log.warn("Intento de acceder a reserva inexistente con ID {}", reservaId);
        //403 si no eres dueño ni admin
        comprobarDuenoOAdmin(r.username());

        log.info("Reserva {} encontrada correctamente", reservaId);
        return r; // 200 ok
    }

    //eliminar alguna reserva por id de reserva
    //204, 401, 403, 404, 409
    @DeleteMapping("/pistaPadel/reservations/{reservaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Reserva eliminar(@PathVariable String reservaId) {

        log.info("Solicitud de cancelación para reserva {}", reservaId);
        Reserva r = obtenerReserva(reservaId);
        log.debug("Reserva encontrada. Usuario: {}, Estado: {}, Fecha: {}",r.username(), r.estado(), r.date());
        comprobarDuenoOAdmin(r.username());

        //409 si ya está cancelada o ya ha pasado la reserva
        if (r.estado() == ReservaStatus.CANCELADA) {
            log.warn("Intento de cancelar reserva ya cancelada. ID: {}", reservaId);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La reserva ya ha sido cancelada");
        }
        if (r.estado() == ReservaStatus.PASADA) {
            log.warn("Intento de cancelar reserva pasada. ID: {}", reservaId);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede cancelar  una reserva pasada");
        }

        //409
        //por política de cancelación si la reserva es en menos de dos horas  no se podrá cancelar
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime inicioReserva= r.date().atTime(APERTURA.plusMinutes((long) r.slotStart() * 30));
        if (Duration.between(ahora,inicioReserva).toMinutes() < 120){
            log.warn("Cancelación rechazada por política de 2 horas. ID: {}, Minutos restantes: {}",
                    reservaId, Duration.between(ahora,inicioReserva).toMinutes());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La reserva no se puede cancelar porque quedan menos de 2 horas");
        }

        //liberar los slots para que vuelvan a estar disponibles para la siguiente reserva
        setSlots(r.courtId(), r.date(), r.slotStart(), r.slotEnd(), false);

        Reserva cancelada = new Reserva(
                r.id(),
                r.username(),
                r.courtId(),
                r.date(),
                r.slotStart(),
                r.slotEnd(),
                ReservaStatus.CANCELADA,
                r.createdAt()
        );
        reservas.put(reservaId, cancelada);
        log.info("Reserva {} cancelada correctamente", reservaId);
        return cancelada;
    }

    //cambiar alguna reserva por id de reserva
    //200, 400, 401, 403, 404, 409
    @PatchMapping("/pistaPadel/reservations/{reservationId}")
    public Reserva cambiar(@PathVariable String reservationId,
                           @RequestBody ReservaPatchRequest req) {

        log.info("Solicitud de modificación de reserva {}", reservationId);

        if (req == null || req.isEmpty()) {
            log.warn("Modificación rechazada: body vacío. Reserva {}", reservationId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se han introducido cambios");
        }
        Reserva actual = obtenerReserva(reservationId);
        log.debug("Reserva actual {} -> usuario={}, pista={}, fecha={}, slots={}..{}, estado={}",
                reservationId, actual.username(), actual.courtId(),
                actual.date(), actual.slotStart(), actual.slotEnd(), actual.estado());

        //409 reserva ya cancelada o reserva pasada (no debería dejar modificarlas)
        if (actual.estado() == ReservaStatus.CANCELADA) {
            log.warn("Modificación rechazada: reserva cancelada. ID {}", reservationId);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede modificar una reserva cancelada");
        }
        if (actual.estado() == ReservaStatus.PASADA) {
            log.warn("Modificación rechazada: reserva pasada. ID {}", reservationId);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede modificar una reserva pasada");
        }

        // construir valores finales a partir de los que han sido cambiados
        String finalDateStr = actual.date().toString();
        if (req.date() != null) finalDateStr = req.date();

        String finalStartStr = APERTURA.plusMinutes((long) actual.slotStart() * 30).toString();
        if (req.startTime() != null) {finalStartStr = req.startTime();}

        int finalDuration = (actual.slotEnd() - actual.slotStart()) * 30;
        if (req.durationMins() != null) {finalDuration = req.durationMins();}

        //validar y calcular nuevos slots
        SlotInfo s = validarYCalcularSlots(finalDateStr, finalStartStr, finalDuration);
        LocalDate newDate = s.date();
        int slotStart = s.slotStart();
        int slotEnd = s.slotEnd();

        //dejar libres antiguas franjas para próximas reservas
        log.debug("Liberando slots antiguos de reserva {}: fecha={}, slots={}..{}",
                reservationId, actual.date(), actual.slotStart(), actual.slotEnd());
        setSlots(actual.courtId(), actual.date(), actual.slotStart(), actual.slotEnd(), false);

        try {
            log.debug("Comprobando disponibilidad para reserva {}: fecha={}, slots={}..{}",
                    reservationId, newDate, slotStart, slotEnd);
            //ver si hay disponibilidad de horario y ocupar nuevas franjas
            disponibilidadSlots(actual.courtId(), newDate, slotStart, slotEnd);
            log.debug("Ocupando nuevos slots para reserva {}: fecha={}, slots={}..{}",
                    reservationId, newDate, slotStart, slotEnd);

            setSlots(actual.courtId(), newDate, slotStart, slotEnd, true);
        } catch (ResponseStatusException ex) {
            log.warn("Modificación fallida ({}). Restaurando slots antiguos. Reserva {}",
                    ex.getStatusCode(), reservationId);
            //si el nuevo rango no está disponible, se restauran los slots antiguos
            setSlots(actual.courtId(), actual.date(), actual.slotStart(), actual.slotEnd(), true);
            throw ex;
        }
        Reserva actualizada = new Reserva(
                actual.id(),
                actual.username(),
                actual.courtId(),
                newDate,
                slotStart,
                slotEnd,
                actual.estado(),
                actual.createdAt()
        );
        reservas.put(reservationId, actualizada);
        log.info("Reserva {} modificada correctamente: fecha {}->{} / slots {}..{} -> {}..{}",
                reservationId, actual.date(), newDate, actual.slotStart(), actual.slotEnd(), slotStart, slotEnd);
        return actualizada; //200 ok
    }

    private Reserva obtenerReserva(String id) {
        Reserva r = reservas.get(id);
        if (r == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe la reserva");  //404
        }
        return r;
    }

    private Pista comprobarPistaExiste(String courtId) {
        Pista pista = servicio.pistas.get(courtId);
        if (pista == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La pista no existe");
        }
        return pista;
    }

    //código usado en post y patch
    //validacion y cálculo de franjas para poder resevar sin solapes
    private SlotInfo validarYCalcularSlots(String dateStr, String startTimeStr, int durationMins) {
        LocalDate date;
        LocalTime start;
        try {
            date = LocalDate.parse(dateStr);
            start = LocalTime.parse(startTimeStr);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato de date/startTime inválido");
        }

        if (date.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes reservar días pasados");
        }

        if (durationMins <= 0 || durationMins % 30 != 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "la duracion en minutos debe ser > 0 y múltiplo de " + 30
            );
        }

        LocalTime end = start.plusMinutes(durationMins);
        if (start.isBefore(APERTURA) || end.isAfter(CIERRE)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fuera de horario (08:00-22:00)");
        }

        int minutesDesdeOpening = (int) Duration.between(APERTURA, start).toMinutes();
        if (minutesDesdeOpening < 0 || minutesDesdeOpening % 30 != 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "startTime debe caer en franjas de 30 min"
            );
        }

        int slotStart = minutesDesdeOpening / 30;
        int slotEnd = slotStart + (durationMins / 30);

        if (slotEnd > 28) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reserva fuera de horario (08:00-22:00)");
        }

        return new SlotInfo(date, slotStart, slotEnd);
    }

    //para franjas horarias (ver si estan o no disponibles)
    private void disponibilidadSlots(String courtId, LocalDate date, int slotStart, int slotEnd) {
        comprobarPistaExiste(courtId);
        ArrayList<Boolean> dia = servicio.obtenerDisponibilidadDia(courtId, date);
        for (int i = slotStart; i < slotEnd; i++) {
            if (dia.get(i)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Slot ocupado");
            }
        }
    }

    private void setSlots(String courtId, LocalDate date, int slotStart, int slotEnd, boolean valor) {
        comprobarPistaExiste(courtId);

        ArrayList<Boolean> dia = servicio.obtenerDisponibilidadDia(courtId, date);
        for (int i = slotStart; i < slotEnd; i++) {
            dia.set(i, valor);
        }
    }

    private void comprobarDuenoOAdmin(String owner) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !owner.equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso");
        }
    }

    //para el control de estados se ha añadido que las reservas cuya fecha ya haya expirado aparezcan como pasadas
    private boolean reservaPasada(Reserva r) {
        LocalDate hoy = LocalDate.now();
        if (r.date().isBefore(hoy)) return true;
        if (r.date().isAfter(hoy)) return false;
        LocalTime fin = APERTURA.plusMinutes((long) r.slotEnd() * 30);
        return !fin.isAfter(LocalTime.now()); // fin <= ahora
    }

    // Ver todas las reservas según determinados filtros
    @GetMapping("/pistaPadel/admin/reservations")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Reserva> obtenerReservas( @RequestParam(required = false) String username,
                                          @RequestParam(required = false) String courtId,
                                          @RequestParam(required = false) LocalDate date) {
        List<Reserva> reservas_solicitadas = new ArrayList<>();
        for (Reserva r : reservas.values()) {
            boolean filtrada = true;

            // si no hay filtro se mantiene
            // en caso de tener filtro si no se cumple la igualdad no se muestra en el return
            if (username != null && !username.equals(r.username())) {
                filtrada = false;
            }
            if (courtId != null && !courtId.equals(r.courtId())) {
                filtrada = false;
            }
            if (date != null && !date.equals(r.date())) {
                filtrada = false;
            }

            if (filtrada) {
                reservas_solicitadas.add(r);
            }
        }

        return reservas_solicitadas;
    }
}