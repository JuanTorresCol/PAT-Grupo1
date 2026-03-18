package edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.controllers;

import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.domain.LoginRequest;
import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.domain.User;
import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.domain.UserCreateRequest;
import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


@RestController
@RequestMapping("/pistaPadel/auth")
public class AuthController {

    @Autowired
    UserService userService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public User register(@Valid @RequestBody UserCreateRequest nuevoUser) {

        return userService.registrar(nuevoUser);
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest loginRequest) {

        return userService.login(loginRequest.getEmail(),loginRequest.getPassword());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestHeader("Authorization") String token) {
        userService.logout(token);
    }

    @GetMapping("/me")
    public User getMe(@RequestHeader("Authorization") String token){
        User user = userService.autentica(token);
        return user;
    }




    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> manejarErrores(RuntimeException ex) {
        //si el error es de login, devolvemos 401 (no autorizado)
        if (ex.getMessage().contains("no encontrado") || ex.getMessage().contains("incorrecta")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
        }
        //para cualquier otro error, devolvemos un 500
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error en el servidor");
    }

}