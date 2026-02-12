package edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Date;

public record Pista(
        @NotBlank(message="Debe rellenar el nombre")
        String idPista,
        @NotBlank(message="Debe rellenar el nombre")
        String nombre,
        @NotBlank(message="Debe rellenar la ubicación")
        String ubicacion,
        @NotNull(message="Debe darle un valor al precio")
        Double precioHora,
        @NotNull(message="Debe establecer un valor de disponibilidad")
        Boolean activa,
        Date fechaAlta
)
{

}
