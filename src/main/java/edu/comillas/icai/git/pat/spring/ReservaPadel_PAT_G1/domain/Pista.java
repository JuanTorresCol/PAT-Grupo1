package edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@Entity
public class Pista {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPista;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(nullable = false)
    private String ubicacion;

    @Column(nullable = false)
    private Double precioHora;

    @Column(nullable = false)
    private Boolean activa;

    private Date fechaAlta = new Date();

    // cons

    public Pista(Long idPista, String nombre, String ubicacion, Double precioHora, Boolean activa, Date fechaAlta) {
        this.idPista = idPista;
        this.nombre = nombre;
        this.ubicacion = ubicacion;
        this.precioHora = precioHora;
        this.activa = activa;
        this.fechaAlta = fechaAlta;
    }

    // mutators and accessors

    public Long getIdPista() {
        return idPista;
    }
    public void setIdPista(Long idPista) {
        this.idPista = idPista;
    }
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public String getUbicacion() {
        return ubicacion;
    }
    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }
    public Double getPrecioHora() {
        return precioHora;
    }
    public void setPrecioHora(Double precioHora) {
        this.precioHora = precioHora;
    }
    public Boolean getActiva() {
        return activa;
    }
    public void setActiva(Boolean activa) {
        this.activa = activa;
    }
    public Date getFechaAlta() {
        return fechaAlta;
    }
    public void setFechaAlta(Date fechaAlta) {
        this.fechaAlta = fechaAlta;
    }
}

