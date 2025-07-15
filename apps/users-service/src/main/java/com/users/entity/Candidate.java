package com.users.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "candidates")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del candidato es obligatorio")
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotBlank(message = "El partido político es obligatorio")
    @Column(name = "partido_politico", nullable = false, length = 100)
    private String partidoPolitico;

    @Column(length = 100)
    private String cargo;

    @Column(length = 20)
    private String color;

    @Column(columnDefinition = "TEXT")
    private String propuestas;

    @Column(columnDefinition = "TEXT")
    private String experiencia;

    @Column(length = 500)
    private String descripcion;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String telefono;

    @Column(name = "lugar_nacimiento", length = 100)
    private String lugarNacimiento;

    @Column(length = 200)
    private String educacion;

    @Column(name = "sitio_web", length = 500)
    private String sitioWeb;

    @Column(length = 500)
    private String imagen;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
