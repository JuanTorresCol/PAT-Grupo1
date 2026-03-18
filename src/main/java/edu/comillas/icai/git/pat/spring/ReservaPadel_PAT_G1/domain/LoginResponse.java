package edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.domain;

public class LoginResponse {
    private String token;
    private Long idUsuario;
    private String nombre;
    private Rol rol;

    public LoginResponse(String token, Long idUsuario, String nombre, Rol rol) {
        this.token = token;
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.rol = rol;
    }

    public String getToken() { return token; }
    public Long getIdUsuario() { return idUsuario; }
    public String getNombre() { return nombre; }
    public Rol getRol() { return rol; }


    public void setToken(String token) { this.token = token; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setRol(Rol rol) { this.rol = rol; }

}