package com.example.academia.DTOs.Response;

import com.example.academia.DTOs.SimpleDTO.AlumnoSimpleDTO;
import com.example.academia.DTOs.SimpleDTO.TareaSimpleDTO;
import com.example.academia.entidades.EntregaEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntregaResponseDTO {
    private Long id;
    private TareaSimpleDTO tarea;
    private AlumnoSimpleDTO alumno;
    private EntregaEntity.EstadoEntrega estado;
    private LocalDateTime fechaEntrega;
    private String nombreDocumento;
    private String tipoDocumento;
    private boolean tieneDocumento;
    private Double nota;
    private String comentarios;

    // Metadatos útiles para el frontend
    private boolean entregadaATiempo;
    private boolean calificada;
}
