package com.example.academia.DTOs;

import com.example.academia.entidades.EntregaEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EntregaDTO {
    private Long id;
    private Long tareaId;
    private Long alumnoId;
    private String nombreAlumno;
    private String apellidoAlumno;
    private LocalDateTime fechaEntrega;
    private String nombreDocumento;
    private Double nota;
    private String comentarios;
    private EntregaEntity.EstadoEntrega estado;
}
