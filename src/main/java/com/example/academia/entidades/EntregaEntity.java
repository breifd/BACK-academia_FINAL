package com.example.academia.entidades;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name="entregas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "entregas"})
public class EntregaEntity {

    public enum EstadoEntrega{
        PENDIENTE,ENTREGADA,CALIFICADA,FUERA_PLAZO;
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarea_id", nullable = false)
    @JsonIgnoreProperties({"alumnosAsignados", "entregas", "curso", "profesor"})
    private TareaEntity tarea;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alumno_id", nullable = false)
    @JsonIgnoreProperties({"cursos", "usuario", "tareasAsignadas", "entregas"})
    private AlumnoEntity alumno;

    @Column(name = "estado")
    @Enumerated(EnumType.STRING)
    private EstadoEntrega estado;

    @Column(name = "fecha_entrega")
    private LocalDateTime fechaEntrega;

    @Lob
    @Column(name = "documento", columnDefinition = "LONGBLOB")
    private byte[] documento;

    @Column(name = "nombre_documento")
    private String nombreDocumento;

    @Column(name = "tipo_documento")
    private String tipoDocumento;

    @Column(name = "nota")
    private Double nota;

    @Column(name = "comentarios")
    private String comentarios;

}
