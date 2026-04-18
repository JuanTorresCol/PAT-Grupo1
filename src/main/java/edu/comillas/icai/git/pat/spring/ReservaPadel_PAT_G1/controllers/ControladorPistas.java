package edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.controllers;

import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.domain.CourtUpdate;
import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.domain.Pista;
import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.domain.User;
import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.services.PistaService;
import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Map;

@RestController
public class ControladorPistas {
    @Autowired
    PistaService pistaService;

    @Autowired
    UserService userService;

    @PostMapping("/pistaPadel/courts")
    @ResponseStatus(HttpStatus.CREATED)

    public Pista creaPista(@Valid @RequestBody Pista pistaNueva, BindingResult bindingResult, @RequestHeader("Authorization") String authHeader) {

        User user = userService.getUserFromHeader(authHeader);
        userService.esAdmin(user);

        return pistaService.creaPista(pistaNueva, bindingResult);
    }

    @GetMapping("/pistaPadel/courts")
    public ArrayList<Pista> listarPistas(
            @RequestParam(required = false) Boolean filtro, @RequestHeader("Authorization") String authHeader) {

        User user = userService.getUserFromHeader(authHeader);

        return pistaService.listarPistas(filtro);
    }

    @GetMapping("/pistaPadel/courts/{nombre}")
    public Pista verDetalle(@PathVariable String nombre, @RequestHeader("Authorization") String authHeader) {

        User user = userService.getUserFromHeader(authHeader);

        return pistaService.verDetalle(nombre);
    }

    @PatchMapping("/pistaPadel/courts/{nombre}")

    public Pista actualizar(@PathVariable String nombre,
                            @RequestBody CourtUpdate newCourt, @RequestHeader("Authorization") String authHeader) {

        User user = userService.getUserFromHeader(authHeader);
        userService.esAdmin(user);

        return pistaService.actuPista(nombre, newCourt);
    }

    @DeleteMapping("/pistaPadel/courts/{nombre}")

    public void delete(@PathVariable String nombre, @RequestHeader("Authorization") String authHeader) {

        User user = userService.getUserFromHeader(authHeader);
        userService.esAdmin(user);

        pistaService.deletePista(nombre);
    }

    @GetMapping("/pistaPadel/availability")
    public Map<Long, ArrayList<Boolean>> checkAvailability(@RequestParam String date, @RequestParam(required = false) String nombre, @RequestHeader("Authorization") String authHeader) {

        User user = userService.getUserFromHeader(authHeader);

        return pistaService.checkAvailability(date, nombre);
    }

    @GetMapping("/pistaPadel/courts/{nombre}/availability")
    public ArrayList<Boolean> checkDispPista(@RequestParam String date, @PathVariable String nombre, @RequestHeader("Authorization") String authHeader) {

        User user = userService.getUserFromHeader(authHeader);

        return pistaService.checkDispPista(date, nombre);
    }
}