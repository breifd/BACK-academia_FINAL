package com.example.academia.DTOs;

import com.example.academia.DTOs.Response.AlumnoResponseDTO;
import com.example.academia.DTOs.Response.ProfesorResponseDTO;
import com.example.academia.DTOs.SimpleDTO.TareaSimpleDTO;
import com.example.academia.entidades.CursoEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CursoConDetallesDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private CursoEntity.NivelCurso nivel;
    private Double precio;

    private List<ProfesorResponseDTO> profesores;
    private List<AlumnoResponseDTO> alumnos;
    private List<TareaSimpleDTO> tareas;

    private int totalProfesores;
    private int totalAlumnos;
    private int totalTareas;
    private int plazasDisponibles;
}