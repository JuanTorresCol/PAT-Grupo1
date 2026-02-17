package edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.controllers;

import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.domain.*;
import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.services.ReservaService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;
import java.util.*;

@RestController
public class ReservaController {

    private static final Logger log = LoggerFactory.getLogger(ReservaController.class);

    private final ReservaService reservaser;
    @Autowired
    public ReservaController(ReservaService reservaser) { this.reservaser = reservaser; }

    //almacenamiento de las reservas
    private Map<String, Reserva> reservas() { return reservaser.reservas(); }

    //slots para los calculos
    private int slotFromTime(LocalTime time) {
        return (int) (Duration.between(ReservaService.APERTURA, time).toMinutes() / 30);
    }

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
        log.debug("Intento de reserva sobre pista inexistente.");
        Pista pista = reservaser.comprobarPistaExiste(req.courtId());
        if (Boolean.FALSE.equals(pista.activa())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La pista no está activa");
        }

        //validación de fecha, hora y cálculo de franjas
        log.debug("Intento de reserva");
        ReservaService.SlotInfo s = reservaser.validarYReservarSlots(req.courtId(),req.date(),req.startTime(),req.durationMins());
        LocalDate date = s.date();
        int slotStart = s.slotStart();
        int slotEnd = s.slotEnd();

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        //horas nuevas
        LocalTime startTime = ReservaService.APERTURA.plusMinutes((long) slotStart * 30);
        LocalTime endTime = ReservaService.APERTURA.plusMinutes((long) slotEnd * 30);

        //201 reserva creada
        Reserva nueva_reserva = new Reserva(
                UUID.randomUUID().toString(), //código id de 36 caracteres
                username,
                req.courtId(),
                date,
                startTime,
                endTime,
                req.durationMins(),
                ReservaStatus.CONFIRMADA,
                Instant.now()
        );
        reservas().put(nueva_reserva.id(), nueva_reserva);
        log.info("Reserva creada correctamente.");
        return nueva_reserva;
    }

    //get todas las reservas de una persona
    // 200, 401
    @GetMapping("/pistaPadel/reservations")
    public List<Reserva> listar() {
        log.debug("Solicitud para listar todas las reservas");

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        List<Reserva> resultado = new ArrayList<>();
        for (Reserva r : reservas().values()) {
            if (!r.username().equals(username)) {continue;}

            if (r.estado() == ReservaStatus.CONFIRMADA && reservaser.reservaPasada(r)) {
                r = new Reserva(r.id(), r.username(), r.courtId(), r.date(),
                        r.startTime(), r.endTime(), r.durationMins(), ReservaStatus.PASADA, r.createdAt());
                reservas().put(r.id(), r);
            }
            resultado.add(r);
        }
        log.info("Se han devuelto reservas");
        return resultado; //200 ok
    }

    //get reservas por id de reserva
    // 200, 401, 403, 404
    @GetMapping("/pistaPadel/reservations/{reservaId}")
    public Reserva buscar(@PathVariable String reservaId) {

        log.debug("Solicitud para obtener reserva con ID {}", reservaId);
        Reserva r = reservaser.obtenerReserva(reservaId);

        //403 si no eres dueño ni admin
        reservaser.comprobarDuenoOAdmin(r.username());

        log.info("Reserva {} encontrada correctamente", reservaId);
        return r; // 200 ok
    }

    //eliminar alguna reserva por id de reserva
    //204, 401, 403, 404, 409
    @DeleteMapping("/pistaPadel/reservations/{reservaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Reserva eliminar(@PathVariable String reservaId) {

        log.info("Solicitud de cancelación para reserva {}", reservaId);
        Reserva r = reservaser.obtenerReserva(reservaId);
        log.debug("Reserva encontrada.");
        reservaser.comprobarDuenoOAdmin(r.username());

        //409 si ya está cancelada o ya ha pasado la reserva
        if (r.estado() == ReservaStatus.CANCELADA) {
            log.warn("Intento de cancelar reserva ya cancelada. ID: {}", reservaId);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La reserva ya ha sido cancelada");
        }
        if (r.estado() == ReservaStatus.PASADA) {
            log.warn("Intento de cancelar reserva pasada. ID: {}", reservaId);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede cancelar  una reserva pasada");
        }
        //409 por política de cancelación si la reserva es en menos de dos horas  no se podrá cancelar
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime inicioReserva = r.date().atTime(r.startTime());
        long mins = Duration.between(ahora, inicioReserva).toMinutes();
        if (mins < 120) {
            log.warn("Cancelación rechazada por política de 2 horas. ID: {}, Minutos restantes: {}",
                    reservaId, mins);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La reserva no se puede cancelar porque quedan menos de 2 horas");
        }

        //liberar los slots para que vuelvan a estar disponibles para la siguiente reserva
        int slotStart = slotFromTime(r.startTime());
        int slotEnd = slotFromTime(r.endTime());
        reservaser.setSlots(r.courtId(), r.date(), slotStart, slotEnd, false);

        Reserva cancelada = new Reserva(
                r.id(),
                r.username(),
                r.courtId(),
                r.date(),
                r.startTime(),
                r.endTime(),
                r.durationMins(),
                ReservaStatus.CANCELADA,
                r.createdAt()
        );
        reservas().put(reservaId, cancelada);
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
        Reserva actual = reservaser.obtenerReserva(reservationId);
        log.debug("Reserva actual {} -> usuario={}, pista={}, fecha={}, slots=?..?, estado={}", reservationId, actual.username(), actual.courtId(), actual.date(), actual.estado());

        //409 reserva ya cancelada o reserva pasada (no debería dejar modificarlas)
        if (actual.estado() == ReservaStatus.CANCELADA) {
            log.warn("Modificación rechazada: reserva cancelada. ID {}", reservationId);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede modificar una reserva cancelada");
        }
        if (actual.estado() == ReservaStatus.PASADA) {
            log.warn("Modificación rechazada: reserva pasada. ID {}", reservationId);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede modificar una reserva pasada");
        }

        //construir valores finales a partir de los que han sido cambiados
        ReservaService.SlotInfo s = reservaser.recalcularYActualizarSlots(actual, req);
        LocalDate newDate = s.date();
        int slotStart = s.slotStart();
        int slotEnd = s.slotEnd();

        //nuevas horas
        LocalTime startTime = ReservaService.APERTURA.plusMinutes((long) slotStart * 30);
        LocalTime endTime = ReservaService.APERTURA.plusMinutes((long) slotEnd * 30);
        int durationMins = (slotEnd - slotStart) * 30;

        Reserva actualizada = new Reserva(
                actual.id(),
                actual.username(),
                actual.courtId(),
                newDate,
                startTime,
                endTime,
                durationMins,
                actual.estado(),
                actual.createdAt()
        );
        reservas().put(reservationId, actualizada);
        log.info("Reserva modificada correctamente");
        return actualizada; //200 ok
    }

    // Ver todas las reservas según determinados filtros
    @GetMapping("/pistaPadel/admin/reservations")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Reserva> obtenerReservas( @RequestParam(required = false) String username,
                                          @RequestParam(required = false) String courtId,
                                          @RequestParam(required = false) LocalDate date) {
        List<Reserva> reservas_solicitadas = new ArrayList<>();
        for (Reserva r : reservaser.reservas().values()) {
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
