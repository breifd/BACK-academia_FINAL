package com.example.academia.DTOs;

import com.example.academia.entidades.UsuarioEntity;
import lombok.Data;

@Data
public class UsuarioDTO {

    private Long id;
    private String username;
    private String password;
    private String nombre;
    private String apellido;
    private UsuarioEntity.Rol rol;
    private Long profesorId;
    private Long alumnoId;
}

