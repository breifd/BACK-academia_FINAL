package com.example.academia.DTOs.SimpleDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlumnoSimpleDTO {
    private Long id;
    private String nombre;
    private String apellido;
    private String email;
}