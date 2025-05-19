package com.example.academia.DTOs;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentoDTO {

    private String nombreArchivo;
    private String tipoArchivo;
    private byte[] contenido;

}
