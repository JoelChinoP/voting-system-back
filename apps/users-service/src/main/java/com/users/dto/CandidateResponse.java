package com.users.dto;

import java.time.LocalDateTime;

public class CandidateResponse {

    private Long id;
    private String nombre;
    private String partidoPolitico;
    private String cargo;
    private String color;
    private String propuestas;
    private String experiencia;
    private String descripcion;
    private String email;
    private String telefono;
    private String lugarNacimiento;
    private String educacion;
    private String sitioWeb;
    private String imagen;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CandidateResponse() {
    }

    public CandidateResponse(Long id, String nombre, String partidoPolitico, String cargo, String color,
                           String propuestas, String experiencia, String descripcion, String email,
                           String telefono, String lugarNacimiento, String educacion, String sitioWeb,
                           String imagen, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.nombre = nombre;
        this.partidoPolitico = partidoPolitico;
        this.cargo = cargo;
        this.color = color;
        this.propuestas = propuestas;
        this.experiencia = experiencia;
        this.descripcion = descripcion;
        this.email = email;
        this.telefono = telefono;
        this.lugarNacimiento = lugarNacimiento;
        this.educacion = educacion;
        this.sitioWeb = sitioWeb;
        this.imagen = imagen;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPartidoPolitico() {
        return partidoPolitico;
    }

    public void setPartidoPolitico(String partidoPolitico) {
        this.partidoPolitico = partidoPolitico;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getPropuestas() {
        return propuestas;
    }

    public void setPropuestas(String propuestas) {
        this.propuestas = propuestas;
    }

    public String getExperiencia() {
        return experiencia;
    }

    public void setExperiencia(String experiencia) {
        this.experiencia = experiencia;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
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

    public String getLugarNacimiento() {
        return lugarNacimiento;
    }

    public void setLugarNacimiento(String lugarNacimiento) {
        this.lugarNacimiento = lugarNacimiento;
    }

    public String getEducacion() {
        return educacion;
    }

    public void setEducacion(String educacion) {
        this.educacion = educacion;
    }

    public String getSitioWeb() {
        return sitioWeb;
    }

    public void setSitioWeb(String sitioWeb) {
        this.sitioWeb = sitioWeb;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
