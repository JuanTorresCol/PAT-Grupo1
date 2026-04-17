package edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.domain;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UserCreateRequest(

    @NotBlank
    String nombre,
    @NotBlank
    String apellidos,          // "YYYY-MM-DD"
    @NotBlank
    @Email(message = "Email inválido")
    String email,     // "HH:mm"
    @NotBlank
    String telefono,
    @NotBlank
    String password)

{

    public String getEmail(){return this.email;}
    public String getNombre(){return nombre;}
    public String getApellidos() {
        return apellidos;
    }
    public String getPassword() {
        return password;
    }
    public String getTelefono() {
        return telefono;
    }



}

