package edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.domain;

import jakarta.validation.constraints.Positive;

public record ReservaPatchRequest(
        String date,
        String startTime,
        @Positive Integer durationMins
) {
    public boolean isEmpty() {
        return date == null && startTime == null && durationMins == null;
    }
}
