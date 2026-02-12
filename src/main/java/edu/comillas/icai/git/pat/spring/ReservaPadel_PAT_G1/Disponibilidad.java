package edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.Date;

public record Disponibilidad(
        @NotBlank(message = "Debe introducir un id de pista")
        String idPista,
        @NotNull(message = "Debe insertar una fecha válida")
        Date fecha,
        @NotNull(message = "Hubo un error con el listado de disponibilidad")
        ArrayList<Boolean> fechasDisponibles
) {
}
