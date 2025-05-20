package com.example.academia.entidades;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tareas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TareaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String nombre;

    @Size(max = 500)
    private String descripcion;

    @Column(name = "fecha_publicacion")
    private LocalDate fechaPublicacion;

    @Column(name = "fecha_limite")
    private LocalDate fechaLimite;


    @Lob
    @Column(name="documento", columnDefinition = "LONGBLOB")
    private byte[] documento;


    @Column(name = "nombre_documento")
    private String nombreDocumento;

    @Column(name = "tipo_documento")
    private String tipoDocumento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curso_id", nullable = false)
    private CursoEntity curso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profesor_id", nullable = false)
    private ProfesorEntity profesor;

    // Si la tarea es para todos los alumnos del curso o solo algunos específicos
    @Column(name = "para_todos_alumnos")
    private Boolean paraTodosLosAlumnos = true;

    // Alumnos específicos asignados si no es para todos
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "tarea_alumnos",
            joinColumns = @JoinColumn(name = "tarea_id"),
            inverseJoinColumns = @JoinColumn(name = "alumno_id")
    )
    private Set<AlumnoEntity> alumnosAsignados = new HashSet<>();

    // Relación inversa con las entregas
    @OneToMany(mappedBy = "tarea", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<EntregaEntity> entregas = new HashSet<>();
}



