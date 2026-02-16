package edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.servicios;

import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.domain.Pista;
import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.domain.Reserva;
import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.domain.ReservaPatchRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ReservaService {

    //horario del sistema (08:00–22:00)
    public static final LocalTime APERTURA = LocalTime.of(8, 0);
    public static final LocalTime CIERRE = LocalTime.of(22, 0);
    //seria el record disponibilidad
    public record SlotInfo(LocalDate date, int slotStart, int slotEnd) {}
    private final PistaService serviciopist;
    //almacenamiento de las reservas
    private final Map<String, Reserva> reservas = new ConcurrentHashMap<>();
    public ReservaService(PistaService servicio) {this.serviciopist = servicio;}
    public Map<String, Reserva> reservas() { return reservas; }

    //metodos usados en el controller

    public Reserva obtenerReserva(String id) {
        Reserva r = reservas.get(id);
        if (r == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe la reserva");  //404
        }
        return r;
    }

    public Pista comprobarPistaExiste(String courtId) {
        Pista pista = serviciopist.getPista(courtId);
        if (pista == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La pista no existe");
        }
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

    //para franjas horarias (ver si estan o no disponibles)
    public void disponibilidadSlots(String courtId, LocalDate date, int slotStart, int slotEnd) {
        comprobarPistaExiste(courtId);
        ArrayList<Boolean> dia = serviciopist.obtenerDisponibilidadDia(courtId, date);
        for (int i = slotStart; i < slotEnd; i++) {
            if (dia.get(i)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Slot ocupado");
            }
        }
    }
    public void setSlots(String courtId, LocalDate date, int slotStart, int slotEnd, boolean valor) {
        comprobarPistaExiste(courtId);
        ArrayList<Boolean> dia = serviciopist.obtenerDisponibilidadDia(courtId, date);
        for (int i = slotStart; i < slotEnd; i++) {
            dia.set(i, valor);
        }
    }

    //para el post
    public SlotInfo validarYReservarSlots(String courtId, String date, String start, int duration){
        //validación de fecha, hora y cálculo de franjas
        SlotInfo s = validarYCalcularSlots(date, start, duration);
        //409 comprobación de slots libres
        disponibilidadSlots(courtId, s.date(), s.slotStart(), s.slotEnd());
        //marcar slots como ocupados
        setSlots(courtId, s.date(), s.slotStart(), s.slotEnd(), true);
        return s;
    }

    //para el patch
    public SlotInfo recalcularYActualizarSlots(Reserva actual, ReservaPatchRequest req) {

        // construir valores finales a partir de los que han sido cambiados
        String finalDateStr = (req.date() != null) ? req.date() : actual.date().toString();
        String finalStartStr = (req.startTime() != null) ? req.startTime() : actual.startTime().toString();
        int finalDuration = (req.durationMins() != null) ? req.durationMins() : actual.durationMins();

        //validar y calcular nuevos slots
        SlotInfo s = validarYCalcularSlots(finalDateStr, finalStartStr, finalDuration);

        //slots antiguos (no guardados)
        int oldSlotStart = (int) (Duration.between(APERTURA, actual.startTime()).toMinutes() / 30);
        int oldSlotEnd = (int) (Duration.between(APERTURA, actual.endTime()).toMinutes() / 30);

        //liberar franjas
        setSlots(actual.courtId(), actual.date(), oldSlotStart, oldSlotEnd, false);

        try {
            //ver si hay disponibilidad de horario y ocupar nuevas franjas
            disponibilidadSlots(actual.courtId(), s.date(), s.slotStart(), s.slotEnd());
            setSlots(actual.courtId(), s.date(), s.slotStart(), s.slotEnd(), true);
        } catch (ResponseStatusException ex) {
            //si el nuevo rango no está disponible, se restauran los slots antiguos
            setSlots(actual.courtId(), actual.date(), oldSlotStart, oldSlotEnd, true);
            throw ex;
        }
        return s;
    }


    public void comprobarDuenoOAdmin(String owner) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        boolean isAdmin = false;
        for (var authority : auth.getAuthorities()) {
            if ("ROLE_ADMIN".equals(authority.getAuthority())) {
                isAdmin = true;
                break;
            }
        }
        if (!isAdmin && !owner.equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso");
        }
    }

    //añadido: para el control de estados se ha añadido que las reservas cuya fecha ya haya expirado aparezcan como pasadas (no modificables)
    public boolean reservaPasada(Reserva r) {
        LocalDate hoy = LocalDate.now();
        if (r.date().isBefore(hoy)) return true;
        if (r.date().isAfter(hoy)) return false;
        LocalTime fin = r.endTime();
        return !fin.isAfter(LocalTime.now()); // fin menor o igual que ahora
    }
}
