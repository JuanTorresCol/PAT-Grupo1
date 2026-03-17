package edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.controllers;

import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.domain.*;
import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.services.ReservaService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
    public Reserva crear(@Valid @RequestBody ReservaCreateRequest req, BindingResult br, Authentication authentication) {
        log.info("Solicitud de creación de reserva recibida");
        if (br.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campos inválidos");
        }
        String username = authentication.getName();
        Reserva nuevaReserva = reservaser.crearReserva(req, username);
        log.info("Reserva creada correctamente.");
        return nuevaReserva;
    }

    //get todas las reservas de una persona
    // 200, 401
    @GetMapping("/pistaPadel/reservations")
    public List<Reserva> listar(Authentication authentication) {
        log.debug("Solicitud para listar todas las reservas");
        String username = authentication.getName();
        List<Reserva> resultado = reservaser.listarReservasUsuario(username);

        log.info("Se han devuelto reservas");
        return resultado;

    }

    //get reservas por id de reserva
    // 200, 401, 403, 404
    @GetMapping("/pistaPadel/reservations/{reservaId}")
    public Reserva buscar(@PathVariable Long reservaId, Authentication authentication) {

        log.debug("Solicitud para obtener reserva con ID {}", reservaId);
        String username = authentication.getName();
        boolean esAdmin = esAdmin(authentication);
        Reserva r = reservaser.buscarReserva(reservaId, username, esAdmin);

        log.info("Reserva {} encontrada correctamente", reservaId);
        return r;
    }

    //eliminar alguna reserva por id de reserva
    //204, 401, 403, 404, 409
    @DeleteMapping("/pistaPadel/reservations/{reservaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long reservaId,  Authentication authentication) {

        log.info("Solicitud de cancelación para reserva {}", reservaId);

        String username = authentication.getName();
        boolean esAdmin = esAdmin(authentication);

        reservaser.cancelarReserva(reservaId, username,esAdmin);

        log.info("Reserva {} cancelada correctamente", reservaId);
    }

    //cambiar alguna reserva por id de reserva
    //200, 400, 401, 403, 404, 409
    @PatchMapping("/pistaPadel/reservations/{reservaId}")
    public Reserva cambiar(@PathVariable Long reservaId,
                           @RequestBody ReservaPatchRequest req, Authentication authentication) {

        log.info("Solicitud de modificación de reserva {}", reservaId);

        if (req == null || req.isEmpty()) {
            log.warn("Modificación rechazada: body vacío. Reserva {}", reservaId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se han introducido cambios");
        }
        String username = authentication.getName();
        boolean esAdmin = esAdmin(authentication);
        Reserva actualizada = reservaser.modificarReserva(reservaId, req, username, esAdmin);

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

    public boolean esAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
