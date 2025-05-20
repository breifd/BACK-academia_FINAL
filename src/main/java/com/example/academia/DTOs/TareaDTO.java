package com.example.academia.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TareaDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private LocalDate fechaPublicacion;
    private LocalDate fechaLimite;
    private Long cursoId;
    private Boolean paraTodosLosAlumnos;
    private List<Long> alumnosIds;
}
