package edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.controllers;

/*
@RestController
@RequestMapping("/pistaPadel/auth")

public class AuthController{
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody Usuario usuario){
        // LLAMADA A X PARA GUARDAR USUARIO
        return ResponseEntity.status(HttpStatus.CREATED).body("Usuario registrado con exito");
    }
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest){
        // LOGICA VALIDACION USUARIO
        return ResponseEntity.ok("Token Simulado");
    }
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(){
        //LOGICA INVALIDACION
        System.out.println("Usuario ha solicitado cierre de sesión");
    }
    @GetMapping("/me")
    public ResponseEntity<Usuario> me(){
        return ResponseEntity.ok(new Usuario());
    }
}

//record para hacer el login
record LoginRequest(String email, String password){}
 */


import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.domain.LoginRequest;
import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.domain.Usuario;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pistaPadel/auth")
public class AuthController{
    private final Map<String, Usuario> usuarios = new HashMap<>();
    private static Long controladorId = 1L; //lo empezamos en 1, esto me va a incrementar el idUsuario
    private Usuario usuarioAutenticado = null;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Usuario register (@Valid @RequestBody Usuario nuevoUsuario, BindingResult bindingResult){
        if (bindingResult.hasErrors()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Datos invalidos"); //400
        }
        if (usuarios.get(nuevoUsuario.getEmail()) != null){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "el email ya existe"); //409
        }
        nuevoUsuario.setIdUsuario(controladorId++); //asigna contador actual y suma uno para el siguiente
        usuarios.put(nuevoUsuario.getEmail(), nuevoUsuario);
        return nuevoUsuario;
    }
    @PostMapping("/login")
    public String login(@RequestBody LoginRequest loginRequest){
        Usuario usuarioExistente = usuarios.get(loginRequest.email());
        if (usuarioExistente != null && usuarioExistente.getPassword().equals(loginRequest.password())){
            this.usuarioAutenticado = usuarioExistente;
            System.out.println("login exitoso para " + loginRequest.email());
            return "login exitoso, bienvenid@ " + usuarioExistente.getNombre();
        }
        //401
        System.out.println("intento de login fallido para " + loginRequest.email());
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "email o password incorrectos");
    }
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT) //204
    public void logout(){
        SecurityContextHolder.clearContext();
    }
    @GetMapping("/me")
    public Usuario me(){
        if (this.usuarioAutenticado != null){
            return this.usuarioAutenticado;
        }else{
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }
}

