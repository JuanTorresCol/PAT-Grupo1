package edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1;

import java.time.Instant;
import java.time.LocalDate;

public record Reserva(
        String id,
        String username,
        String courtId,
        LocalDate date,
        int slotStart,
        int slotEnd,
        ReservaStatus estado,
        Instant createdAt
) {}
