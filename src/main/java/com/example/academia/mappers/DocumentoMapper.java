package com.example.academia.mappers;

import com.example.academia.DTOs.DocumentoDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DocumentoMapper {
    DocumentoDTO toDocumentoDTO(String nombreArchivo, String tipoArchivo, byte[] contenido);
}