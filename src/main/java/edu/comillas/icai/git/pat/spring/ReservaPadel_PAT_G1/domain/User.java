package edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Builder //Genera los constructores
@Data //Genera los getters y setters
@NoArgsConstructor  // constructor vacío requerido por JPA
@AllArgsConstructor // necesario para que @Builder funcione correctamente si no dan fallos
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsuario;

    @NotBlank
    @Column(nullable = false)
    private String nombre;

    @NotBlank
    @Column(nullable = false)
    private String apellidos;

    @NotBlank
    @Email
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // No devolver password en respuestas JSON
    @Column(nullable = false)
    private String password;

    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;

    @Column(nullable = false)
    private LocalDate fechaRegistro;

    @Column(nullable = false)
    private boolean activo;


}