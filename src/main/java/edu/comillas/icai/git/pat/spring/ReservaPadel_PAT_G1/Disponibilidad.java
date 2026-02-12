package edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public record Disponibilidad(
        @NotBlank(message = "Debe introducir un id de pista")
        String idPista,

        @NotNull(message = "Debe insertar una fecha válida")
        Date fecha,

        ArrayList<Boolean> fechasDisponibles
) {

    public Disponibilidad {
        if (fechasDisponibles == null) {
            fechasDisponibles = new ArrayList<>(Collections.nCopies(28, false));
        }
    }
}

