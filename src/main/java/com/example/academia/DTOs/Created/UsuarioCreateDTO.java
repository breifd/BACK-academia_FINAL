package com.example.academia.DTOs.Created;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioCreateDTO {
    private String username;
    private String password;
    private String nombre;
    private String apellido;
}
