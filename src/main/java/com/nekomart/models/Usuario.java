package com.nekomart.models;

/**
 * Clase Modelo (POJO) que representa a un Usuario en el sistema NekoMart.
 */
public class Usuario {
    private int id;
    private String username;
    private String passwordHash;
    private String rol; // Valores esperados: "ADMIN", "EMPLEADO"
    private String nombreCompleto;
    private String fotoRuta; // Ruta de la foto de perfil del usuario
    private String email; // Correo electrónico del usuario
    private String telefono; // Teléfono de contacto del usuario

    // Constructor vacío
    public Usuario() {
    }

    // Constructor con todos los campos excepto ID (útil para inserciones)
    public Usuario(String username, String passwordHash, String rol, String nombreCompleto) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.nombreCompleto = nombreCompleto;
    }

    // Constructor completo (útil para recuperaciones)
    public Usuario(int id, String username, String passwordHash, String rol, String nombreCompleto) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.nombreCompleto = nombreCompleto;
    }

    // Getters y Setters con documentación

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getFotoRuta() {
        return fotoRuta;
    }

    public void setFotoRuta(String fotoRuta) {
        this.fotoRuta = fotoRuta;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", rol='" + rol + '\'' +
                ", nombreCompleto='" + nombreCompleto + '\'' +
                ", fotoRuta='" + fotoRuta + '\'' +
                '}';
    }
}
