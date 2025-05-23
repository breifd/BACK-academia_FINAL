package com.example.academia.mappers;

import com.example.academia.DTOs.Created.EntregaCreateDTO;
import com.example.academia.DTOs.Response.EntregaResponseDTO;
import com.example.academia.DTOs.SimpleDTO.EntregaSimpleDTO;
import com.example.academia.entidades.EntregaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {TareaMapper.class, AlumnoMapper.class})
public interface EntregaMapper {

    @Mapping(target = "tieneDocumento", expression = "java(entrega.getDocumento() != null && entrega.getDocumento().length > 0)")
    @Mapping(target = "entregadaATiempo", expression = "java(entrega.getEstado() != EntregaEntity.EstadoEntrega.FUERA_PLAZO)")
    @Mapping(target = "calificada", expression = "java(entrega.getEstado() == EntregaEntity.EstadoEntrega.CALIFICADA)")
    EntregaResponseDTO toEntregaResponseDTO(EntregaEntity entrega);

    @Mapping(target = "tareaId", source = "tarea.id")
    @Mapping(target = "alumnoId", source = "alumno.id")
    @Mapping(target = "nombreAlumno", source = "alumno.nombre")
    @Mapping(target = "apellidoAlumno", source = "alumno.apellido")
    EntregaSimpleDTO toEntregaSimpleDTO(EntregaEntity entrega);

    List<EntregaResponseDTO> toEntregaResponseDTOList(List<EntregaEntity> entregas);

    List<EntregaSimpleDTO> toEntregaSimpleDTOList(List<EntregaEntity> entregas);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "documento", ignore = true)
    @Mapping(target = "nombreDocumento", ignore = true)
    @Mapping(target = "tipoDocumento", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaEntrega", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "nota", ignore = true)
    EntregaEntity toEntregaEntity(EntregaCreateDTO dto);

    // Método por defecto para establecer el estado inicial
    default EntregaEntity establecerEstadoInicial(EntregaEntity entrega) {
        entrega.setEstado(EntregaEntity.EstadoEntrega.PENDIENTE);
        return entrega;
    }

}