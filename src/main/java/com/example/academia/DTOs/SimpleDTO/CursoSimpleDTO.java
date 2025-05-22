package com.example.academia.DTOs.SimpleDTO;
import com.example.academia.entidades.CursoEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CursoSimpleDTO {
    private Long id;
    private String nombre;
    private CursoEntity.NivelCurso nivel;
}