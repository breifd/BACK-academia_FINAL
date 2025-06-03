package com.example.academia.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CambioPasswordDTO {
    private String passwordActual;
    private String passwordNueva;

}
