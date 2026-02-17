package edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1;


import java.time.LocalDateTime;

public class Usuario{
    private Long idUsuario;
    private String nombre;
    private String apellidos;
    private String email;
    private String password;
    private String telefono;
    private Rol rol;
    private LocalDateTime fechaRegistro;
    private boolean activo;

    //constructor vacio
    public Usuario(){
        this.fechaRegistro = LocalDateTime.now();
        this.activo = true;
    }

    //constructor completo - crear usuarios en registro
    public Usuario(Long idUsuario, String nombre, String apellidos, String email, String password, String telefono, Rol rol){
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
        this.password = password;
        this.telefono = telefono;
        this.rol = rol;
        this.fechaRegistro = LocalDateTime.now();
        this.activo = true;
    }

    //getters y setters
    public Long getIdUsuario(){return idUsuario;}
    public void setIdUsuario(Long idUsuario){this.idUsuario = idUsuario;}
    public String getNombre(){return nombre;}
    public void setNombre(String nombre){this.nombre = nombre;}
    public String getApellidos(){return apellidos;}
    public void setApellidos(String apellidos){this.apellidos = apellidos;}
    public String getEmail(){return email;}
    public void setEmail(String email){this.email = email;}
    public String getPassword(){return password;}
    public void setPassword(String password){this.password = password;}
    public String getTelefono(){return telefono;}
    public void setTelefono(String telefono){this.telefono = telefono;}
    public Rol getRol(){return rol;}
    public void setRol(Rol rol){this.rol = rol;}
    public LocalDateTime getFechaRegistro(){return fechaRegistro;}
    public boolean isActivo(){return activo;}
}