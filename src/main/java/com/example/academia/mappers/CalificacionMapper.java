package com.example.academia.mappers;

import com.example.academia.DTOs.CalificacionDTO;
import com.example.academia.entidades.EntregaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CalificacionMapper {
    @Mapping(target = "nota", source = "calificacion.nota")
    @Mapping(target = "comentarios", source = "calificacion.comentarios")
    void updateEntregaFromCalificacion(CalificacionDTO calificacion, @MappingTarget EntregaEntity entrega);
}