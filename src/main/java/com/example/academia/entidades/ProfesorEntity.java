package com.example.academia.entidades;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "profesores")
@Data
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"usuario", "cursos"})
@AllArgsConstructor
public class ProfesorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String nombre;

    @NotBlank
    @Size(max = 100)
    private String apellido;

    @Size(max = 20)
    private String telefono;

    @Size(max = 100)
    private String email;

    @Size(max = 100)
    private String especialidad;

    @Column(name = "anhos_experiencia")
    private Integer anhosExperiencia;

    @OneToOne(mappedBy = "profesor")
    @JsonIgnore
    private UsuarioEntity usuario;

    @ManyToMany(mappedBy = "profesores", fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"profesores", "usuario", "tareas"})
    private Set<CursoEntity> cursos = new HashSet<>();

    @OneToMany(mappedBy = "profesor")
    @JsonIgnore
    private Set<TareaEntity> tareas = new HashSet<>();


}
