package edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.controllers;

import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.domain.CourtUpdate;
import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.domain.Pista;
import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.domain.User;
import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.repositories.PistaRepository;
import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.services.PistaService;
import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.services.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class ControladorPistas {
    @Autowired
    PistaService pistaService;
    //private final Map<Long, Map<LocalDate, ArrayList<Boolean>>> disponibilidades = new ConcurrentHashMap<>();
    //private static final Logger log = LoggerFactory.getLogger(ControladorPistas.class);

    @Autowired
    UserService userService;

    @PostMapping("/pistaPadel/courts")
    @ResponseStatus(HttpStatus.CREATED)

    public Pista creaPista(@Valid @RequestBody Pista pistaNueva,
                           BindingResult bindingResult, @RequestHeader("Authorization") String token) {
        User user = userService.autentica(token);
        userService.esAdmin(user);

        return pistaService.creaPista(pistaNueva, bindingResult);
    }

    @GetMapping("/pistaPadel/courts")
    public ArrayList<Pista> listarPistas(
            @RequestParam(required = false) Boolean filtro, @RequestHeader("Authorization") String token) {

        User user = userService.autentica(token);

        return pistaService.listarPistas(filtro);
    }

    @GetMapping("/pistaPadel/courts/{nombre}")
    public Pista verDetalle(@PathVariable String nombre, @RequestHeader("Authorization") String token) {

        User user = userService.autentica(token);

        return pistaService.verDetalle(nombre);
    }

    @PatchMapping("/pistaPadel/courts/{nombre}")

    public Pista actualizar(@PathVariable String nombre,
                            @RequestBody CourtUpdate newCourt, @RequestHeader("Authorization") String token) {

        User user = userService.autentica(token);
        userService.esAdmin(user);

        return pistaService.actuPista(nombre, newCourt);
    }

    @DeleteMapping("/pistaPadel/courts/{nombre}")

    public void delete(@PathVariable String nombre, @RequestHeader("Authorization") String token) {

        User user = userService.autentica(token);
        userService.esAdmin(user);

        pistaService.deletePista(nombre);
    }

    @GetMapping("/pistaPadel/availability")
    public Map<Long, ArrayList<Boolean>> checkAvailability(@RequestParam String date, @RequestParam(required = false) String nombre, @RequestHeader("Authorization") String token) {

        User user = userService.autentica(token);

        return pistaService.checkAvailability(date, nombre);
    }

    @GetMapping("/pistaPadel/courts/{nombre}/availability")
    public ArrayList<Boolean> checkDispPista(@RequestParam String date, @PathVariable String nombre, @RequestHeader("Authorization") String token) {

        User user = userService.autentica(token);

        return pistaService.checkDispPista(date, nombre);
    }
}