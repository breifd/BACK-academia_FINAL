package com.example.academia.DTOs.SimpleDTO;

import com.example.academia.entidades.EntregaEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntregaSimpleDTO {
    private Long id;
    private Long tareaId;
    private Long alumnoId;
    private String nombreAlumno;
    private String apellidoAlumno;
    private LocalDateTime fechaEntrega;
    private EntregaEntity.EstadoEntrega estado;
    private Double nota;
}