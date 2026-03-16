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

    @Autowired
    ReservaService reservaser;


    //crear reserva
    //respuesta: 201, 400, 401, 404, 409
    @PostMapping("/pistaPadel/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    public Reserva crear(@Valid @RequestBody ReservaCreateRequest req, BindingResult br) {
        log.info("Solicitud de creación de reserva recibida");
        if (br.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campos inválidos");
        }
        Reserva nuevaReserva = reservaser.crearReserva(req);
        log.info("Reserva creada correctamente.");
        return nuevaReserva;
    }

    //get todas las reservas de una persona
    // 200, 401
    @GetMapping("/pistaPadel/reservations")
    public List<Reserva> listar() {
        log.debug("Solicitud para listar todas las reservas");

        List<Reserva> resultado = reservaser.listarReservasUsuario();

        log.info("Se han devuelto reservas");
        return resultado;

    }

    //get reservas por id de reserva
    // 200, 401, 403, 404
    @GetMapping("/pistaPadel/reservations/{reservaId}")
    public Reserva buscar(@PathVariable Long reservaId) {

        log.debug("Solicitud para obtener reserva con ID {}", reservaId);

        Reserva r = reservaser.buscarReserva(reservaId);

        log.info("Reserva {} encontrada correctamente", reservaId);
        return r;
    }

    //eliminar alguna reserva por id de reserva
    //204, 401, 403, 404, 409
    @DeleteMapping("/pistaPadel/reservations/{reservaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long reservaId) {

        log.info("Solicitud de cancelación para reserva {}", reservaId);

        reservaser.cancelarReserva(reservaId);

        log.info("Reserva {} cancelada correctamente", reservaId);
    }

    //cambiar alguna reserva por id de reserva
    //200, 400, 401, 403, 404, 409
    @PatchMapping("/pistaPadel/reservations/{reservaId}")
    public Reserva cambiar(@PathVariable Long reservaId,
                           @RequestBody ReservaPatchRequest req) {

        log.info("Solicitud de modificación de reserva {}", reservaId);

        if (req == null || req.isEmpty()) {
            log.warn("Modificación rechazada: body vacío. Reserva {}", reservaId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se han introducido cambios");
        }

        Reserva actualizada = reservaser.modificarReserva(reservaId, req);

        log.info("Reserva modificada correctamente");
        return actualizada;
    }

    // Ver todas las reservas según determinados filtros
    @GetMapping("/pistaPadel/admin/reservations")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Reserva> obtenerReservas(@RequestParam(required = false) String username,
                                         @RequestParam(required = false) String courtId,
                                         @RequestParam(required = false) LocalDate date) {
        return reservaser.obtenerReservas(username, courtId, date);
    }
}
