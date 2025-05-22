package com.example.academia.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticasDTO {
    private int totalAlumnos;
    private int totalProfesores;
    private int totalCursos;
    private int totalTareas;
    private int entregasPendientes;
    private int usuariosActivos;
}
