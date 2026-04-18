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
    public void logout(@RequestHeader("Authorization") String authHeader) {
        User user = userService.getUserFromHeader(authHeader);
        userService.logout(authHeader);
    }

    @GetMapping("/me")
    public User getMe(@RequestHeader("Authorization") String authHeader){
        User user = userService.getUserFromHeader(authHeader);
        return userService.autentica(authHeader);
    }





}