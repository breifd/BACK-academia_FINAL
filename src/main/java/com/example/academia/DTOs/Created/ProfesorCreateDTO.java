package com.example.academia.DTOs.Created;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfesorCreateDTO {
    private String nombre;
    private String apellido;
    private String telefono;
    private String email;
    private String especialidad;
    private Integer anhosExperiencia;

    // Para crear usuario asociado (opcional)
    private UsuarioCreateDTO usuario;
}