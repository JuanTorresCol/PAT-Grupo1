package edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Builder //Genera los constructores
@Data //Genera los getters y setters
@NoArgsConstructor  // constructor vacío requerido por JPA
@AllArgsConstructor // necesario para que @Builder funcione correctamente si no dan fallos

@Entity
public class Reserva{
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        @ManyToOne@JoinColumn(unique = false)
        private User username;

        @ManyToOne@JoinColumn(unique = false)
        private Pista pista;

        @Column(nullable = false, unique = false)
        private LocalDate date;
        @Column(nullable = false, unique = false)
        private LocalTime startTime;
        @Column(nullable = false, unique = false)
        private LocalTime endTime;
        @Column(nullable = false, unique = false)
        private int durationMins;
        @Column(nullable = false, unique = false)
        private ReservaStatus estado;
        @Column(nullable = false, unique = false)
        private Instant createdAt;
 }
