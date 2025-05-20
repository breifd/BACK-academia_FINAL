package com.example.academia.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntregaRequestDTO {
    private Long tareaId;
    private Long alumnoId;
    private String comentarios;
}