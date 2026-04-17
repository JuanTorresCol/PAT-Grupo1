package edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.domain;

import jakarta.validation.constraints.Email;

public record UserPatchRequest(

        @Email(message = "Email inválido")
        String email,

        String nombre,

        String apellidos,

        String telefono,
        Boolean active

) {

    public boolean isEmpty() {
        return email == null && nombre == null && apellidos==null && telefono==null && active == null;
    }
}
