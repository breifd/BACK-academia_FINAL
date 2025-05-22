package com.example.academia.DTOs;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionCursoDTO {
    private Long cursoId;
    private Long personaId; // puede ser profesorId o alumnoId
    private String accion; // "ASIGNAR" o "DESASIGNAR"
}
