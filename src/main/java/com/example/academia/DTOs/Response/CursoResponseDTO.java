package com.example.academia.DTOs.Response;

import com.example.academia.DTOs.SimpleDTO.AlumnoSimpleDTO;
import com.example.academia.DTOs.SimpleDTO.ProfesorSimpleDTO;
import com.example.academia.entidades.CursoEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CursoResponseDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private CursoEntity.NivelCurso nivel;
    private Double precio;

    // Solo IDs y nombres para evitar recursión
    private List<ProfesorSimpleDTO> profesores;
    private List<AlumnoSimpleDTO> alumnos;

    // Estadísticas útiles para el frontend
    private int totalProfesores;
    private int totalAlumnos;
    private int totalTareas;
}