package com.example.academia.entidades;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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

    @Min(0)
    @Max(10)
    private Double nota;

    //Verificamos si el documento está cargado
    public boolean isDocumentoCargado() {
        return documento != null && documento.length > 0;
    }
    @PrePersist
    @PreUpdate
    public void calcularNota() {
        if (!isDocumentoCargado() && fechaLimite != null && LocalDate.now().isAfter(fechaLimite)) {
            nota = null; //Contará como que no lo ha entregado
        }
    }
}