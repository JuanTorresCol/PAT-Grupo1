package edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReservaCreateRequest(
        @NotBlank
        String courtId,
        @NotBlank
        String date,          // "YYYY-MM-DD"
        @NotBlank
        String startTime,     // "HH:mm"
        @NotNull
        @Positive Integer durationMins
) {}
