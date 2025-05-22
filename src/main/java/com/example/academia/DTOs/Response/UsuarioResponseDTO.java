package com.example.academia.DTOs.Response;

import com.example.academia.DTOs.SimpleDTO.AlumnoSimpleDTO;
import com.example.academia.DTOs.SimpleDTO.ProfesorSimpleDTO;
import com.example.academia.entidades.UsuarioEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponseDTO {
    private Long id;
    private String username;
    private String nombre;
    private String apellido;
    private UsuarioEntity.Rol rol;

    // Info básica sin recursión
    private ProfesorSimpleDTO profesor;
    private AlumnoSimpleDTO alumno;
}