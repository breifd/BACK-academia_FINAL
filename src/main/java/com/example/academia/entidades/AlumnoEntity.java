package com.example.academia.entidades;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Table(name = "alumnos")
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"usuario", "cursos"})
@NoArgsConstructor
public class AlumnoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String nombre;

    @NotBlank
    @Size(max = 100)
    private String apellido;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Size(max = 20)
    private String telefono;

    @Size(max = 100)
    private String email;

    @Size(max = 200)
    private String direccion;

    @OneToOne(mappedBy = "alumno")
    @JsonIgnore
    private UsuarioEntity usuario;

    @ManyToMany(mappedBy = "alumnos", fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"profesores","alumnos"})
    private Set<CursoEntity> cursos = new HashSet<>();

    @ManyToMany(mappedBy = "alumnosAsignados")
    @JsonIgnore
    private Set<TareaEntity> tareasAsignadas = new HashSet<>();

    @OneToMany(mappedBy = "alumno")
    @JsonIgnore
    private Set<EntregaEntity> entregas = new HashSet<>();
}
