package com.example.academia.DTOs.SimpleDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TareaSimpleDTO {
    private Long id;
    private String nombre;
    private LocalDate fechaLimite;
    private Boolean paraTodosLosAlumnos;
    private ProfesorSimpleDTO profesor;
    private CursoSimpleDTO curso;
}

