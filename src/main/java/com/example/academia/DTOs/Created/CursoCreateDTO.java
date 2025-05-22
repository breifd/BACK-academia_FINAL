package com.example.academia.DTOs.Created;

import com.example.academia.entidades.CursoEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CursoCreateDTO {
    private String nombre;
    private String descripcion;
    private CursoEntity.NivelCurso nivel;
    private Double precio;
}