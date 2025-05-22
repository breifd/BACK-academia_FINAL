package com.example.academia.DTOs.Response;

import com.example.academia.DTOs.SimpleDTO.AlumnoSimpleDTO;
import com.example.academia.DTOs.SimpleDTO.CursoSimpleDTO;
import com.example.academia.DTOs.SimpleDTO.ProfesorSimpleDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TareaResponseDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private LocalDate fechaPublicacion;
    private LocalDate fechaLimite;
    private Boolean paraTodosLosAlumnos;
    private String nombreDocumento;
    private boolean tieneDocumento;

    // Info básica sin recursión
    private CursoSimpleDTO curso;
    private ProfesorSimpleDTO profesor;
    private List<AlumnoSimpleDTO> alumnosAsignados;

    // Estadísticas para el frontend
    private int totalEntregas;
    private int entregasPendientes;
}