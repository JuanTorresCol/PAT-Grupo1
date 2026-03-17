package edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.services;

import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.domain.*;
import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.repositories.PistaRepository;
import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.repositories.ReservaRepository;
import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReservaService {


    //horario del sistema (08 –22)
    public static final LocalTime APERTURA = LocalTime.of(8, 0);
    public static final LocalTime CIERRE = LocalTime.of(22, 0);
    //seria el record disponibilidad
    public record SlotInfo(LocalDate date, int slotStart, int slotEnd) {}

    @Autowired
    ReservaRepository reporeserva;
    @Autowired
    PistaRepository repopista;
    @Autowired
    UserRepository repouser;


    public Reserva obtenerReserva(Long id) {
        return reporeserva.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe la reserva"));
    }


    public Reserva crearReserva(ReservaCreateRequest req, String username) {
        Pista pista = comprobarPistaExiste(req.getCourtId());
        if (Boolean.FALSE.equals(pista.getActiva())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La pista no está activa");
        }

        SlotInfo s = validarYCalcularSlots(req.date(), req.startTime(), req.durationMins());

        User usuario = repouser.findByEmail(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));


        LocalTime startTime = APERTURA.plusMinutes((long) s.slotStart() * 30);
        LocalTime endTime = APERTURA.plusMinutes((long) s.slotEnd() * 30);

        comprobarSolapeBD(req.getCourtId(), s.date(), startTime, endTime, null);

        Reserva nuevaReserva = new Reserva();
        nuevaReserva.setUsername(usuario);
        nuevaReserva.setPista(pista);
        nuevaReserva.setDate(s.date());
        nuevaReserva.setStartTime(startTime);
        nuevaReserva.setEndTime(endTime);
        nuevaReserva.setDurationMins(req.durationMins());
        nuevaReserva.setEstado(ReservaStatus.CONFIRMADA);
        nuevaReserva.setCreatedAt(Instant.now());

        return reporeserva.save(nuevaReserva);
    }

    public List<Reserva> listarReservasUsuario(String username) {

        List<Reserva> reservasUsuario = reporeserva.findByUsernameEmail(username);
        List<Reserva> resultado = new ArrayList<>();

        for (Reserva r : reservasUsuario) {
            if (r.getEstado() == ReservaStatus.CONFIRMADA && reservaPasada(r)) {
                r.setEstado(ReservaStatus.PASADA);
                r = reporeserva.save(r);
            }
            resultado.add(r);
        }
        return resultado;
    }

    public Reserva buscarReserva(Long reservaId, String username, boolean esAdmin) {
        Reserva r = obtenerReserva(reservaId);
        comprobarDuenoOAdmin(r.getUsername().getEmail(), username, esAdmin);


        if (r.getEstado() == ReservaStatus.CONFIRMADA && reservaPasada(r)) {
            r.setEstado(ReservaStatus.PASADA);
            r = reporeserva.save(r);
        }

        return r;

    }


    public void cancelarReserva(Long reservaId, String username, boolean esAdmin) {
        Reserva r = obtenerReserva(reservaId);
        comprobarDuenoOAdmin(r.getUsername().getEmail(), username, esAdmin);


        if (r.getEstado() == ReservaStatus.CANCELADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La reserva ya ha sido cancelada");
        }

        if (r.getEstado() == ReservaStatus.PASADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede cancelar una reserva pasada");
        }

        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime inicioReserva = r.getDate().atTime(r.getStartTime());
        long mins = Duration.between(ahora, inicioReserva).toMinutes();

        //por politica si quedan menos de dos horas no deja reservar
        if (mins < 120) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La reserva no se puede cancelar porque quedan menos de 2 horas");
        }

        r.setEstado(ReservaStatus.CANCELADA);

        reporeserva.save(r);
    }

    public Reserva modificarReserva(Long reservaId, ReservaPatchRequest req, String username, boolean esAdmin) {
        Reserva actual = obtenerReserva(reservaId);
        comprobarDuenoOAdmin(actual.getUsername().getEmail(), username, esAdmin);


        if (actual.getEstado() == ReservaStatus.CANCELADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede modificar una reserva cancelada");
        }

        if (actual.getEstado() == ReservaStatus.PASADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede modificar una reserva pasada");
        }

        SlotInfo s = recalcularSlots(actual, req);


        LocalTime startTime = APERTURA.plusMinutes((long) s.slotStart() * 30);
        LocalTime endTime = APERTURA.plusMinutes((long) s.slotEnd() * 30);
        int durationMins = (s.slotEnd() - s.slotStart()) * 30;

        comprobarSolapeBD(actual.getPista().getIdPista(), s.date(), startTime, endTime, actual.getId());

        actual.setDate(s.date());
        actual.setStartTime(startTime);
        actual.setEndTime(endTime);
        actual.setDurationMins(durationMins);

        return reporeserva.save(actual);
    }

    public List<Reserva> obtenerReservas(String username, String courtId, LocalDate date) {
        Iterable<Reserva> todas = reporeserva.findAll(); //CRUD devuelve iterable no List
        List<Reserva> resultado = new ArrayList<>();

        for (Reserva r : todas) {
            boolean filtrada = true;

            if (username != null && !username.equals(r.getUsername().getEmail())) {
                filtrada = false;
            }

            if (courtId != null && !courtId.equals(String.valueOf(r.getPista().getIdPista()))) {
                filtrada = false;
            }

            if (date != null && !date.equals(r.getDate())) {
                filtrada = false;
            }

            if (filtrada) {
                resultado.add(r);
            }
        }

        return resultado;
    }


    public Pista comprobarPistaExiste(Long courtId) {
        Pista pista = repopista.findById(courtId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "La pista no existe"));

        return pista;
    }

    //toda la lógica de las franjas
    //validacion y cálculo de franjas para poder resevar sin solapes
    public SlotInfo validarYCalcularSlots(String dateStr, String startTimeStr, int durationMins) {
        LocalDate date;
        LocalTime start;
        try {
            date = LocalDate.parse(dateStr);
            start = LocalTime.parse(startTimeStr);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato de date/startTime inválido");
        }
        LocalTime end = start.plusMinutes(durationMins);
        if (date.isBefore(LocalDate.now())|| date.equals(LocalDate.now()) && !end.isAfter(LocalTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes reservar días ni horas que ya han pasado");
        }
        if (durationMins <= 0 || durationMins % 30 != 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "la duracion en minutos debe ser > 0 y múltiplo de " + 30
            );
        }
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

        return new SlotInfo(date, slotStart, slotEnd);
    }



    //para el patch
    public SlotInfo recalcularSlots(Reserva actual, ReservaPatchRequest req) {

        // construir valores finales a partir de los que han sido cambiados
        String finalDateStr = (req.date() != null) ? req.date() : actual.getDate().toString();
        String finalStartStr = (req.startTime() != null) ? req.startTime() : actual.getStartTime().toString();
        int finalDuration = (req.durationMins() != null) ? req.durationMins() : actual.getDurationMins();

        return validarYCalcularSlots(finalDateStr, finalStartStr, finalDuration);

    }

    public void comprobarSolapeBD(Long pistaId, LocalDate date, LocalTime nuevaInicio, LocalTime nuevaFin, Long reserva_actualId) {
        List<Reserva> reservas = reporeserva.findByPistaIdPistaAndDate(pistaId, date);
        for (Reserva r : reservas) {
            if (r.getEstado() == ReservaStatus.CANCELADA) {continue;}
            if (reserva_actualId != null && r.getId().equals(reserva_actualId)) {
                continue;
                }
                boolean solapa = nuevaInicio.isBefore(r.getEndTime()) && nuevaFin.isAfter(r.getStartTime());
                if (solapa) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Slot ocupado");
                }
        }
    }

    public void comprobarDuenoOAdmin(String usuario_dueno, String usuario_act, boolean esAdmin) {
        if (!esAdmin && !usuario_dueno.equals(usuario_act)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso");
        }
    }

    //añadido: para el control de estados se ha añadido que las reservas cuya fecha ya haya expirado aparezcan como pasadas (no modificables)
    public boolean reservaPasada(Reserva r) {
        LocalDate hoy = LocalDate.now();
        if (r.getDate().isBefore(hoy)) return true;
        if (r.getDate().isAfter(hoy)) return false;
        LocalTime fin = r.getEndTime();
        return !fin.isAfter(LocalTime.now()); // fin menor o igual que ahora
    }
}
