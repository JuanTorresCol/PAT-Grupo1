package edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public record Reserva(
        String id,
        String username,
        String courtId,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        int durationMins,
        ReservaStatus estado,
        Instant createdAt
) {}
