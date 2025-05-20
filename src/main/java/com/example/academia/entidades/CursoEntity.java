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

@Data
@Entity
@Table(name="cursos")
@AllArgsConstructor

@NoArgsConstructor
public class CursoEntity{


    public enum NivelCurso{
        Básico, Intermedio, Avanzado, Experto;
    }

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String nombre;

    @Size(max = 500)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel")
    private NivelCurso nivel;

    @Column(name = "precio")
    private Double precio;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "curso_profesores",
            joinColumns = @JoinColumn(name = "curso_id"),
            inverseJoinColumns = @JoinColumn(name = "profesor_id")
    )
    @JsonIgnoreProperties({"cursos", "usuario"})
    private Set<ProfesorEntity> profesores = new HashSet<>();
    //*Cuando serialices un ProfesorEntity dentro de este contexto, ignora sus propiedades cursos y usuario. No las incluyas en el JSON.
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "curso_alumnos",
            joinColumns = @JoinColumn(name = "curso_id"),
            inverseJoinColumns = @JoinColumn(name = "alumno_id")
    )
    @JsonIgnoreProperties({"cursos", "usuario"})
    private Set<AlumnoEntity> alumnos = new HashSet<>();

    @OneToMany(mappedBy = "curso")
    @JsonIgnore
    private Set<TareaEntity> tareas = new HashSet<>();

}
