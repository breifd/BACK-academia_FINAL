package com.example.academia.DTOs.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfesorResponseDTO {
    private Long id;
    private String nombre;
    private String apellido;
    private String telefono;
    private String email;
    private String especialidad;
    private Integer anhosExperiencia;

    // Info del usuario (si tiene)
    private String username;
    private boolean tieneUsuario;
}