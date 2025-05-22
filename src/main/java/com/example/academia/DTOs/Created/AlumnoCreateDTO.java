package com.example.academia.DTOs.Created;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlumnoCreateDTO {
    private String nombre;
    private String apellido;
    private LocalDate fechaNacimiento;
    private String telefono;
    private String email;
    private String direccion;

    // Para crear usuario asociado (opcional)
    private UsuarioCreateDTO usuario;
}
