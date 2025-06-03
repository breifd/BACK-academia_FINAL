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
    @Mapping(target = "tieneDocumentoProfesor", expression = "java(entrega.getDocumentoProfesor() != null && entrega.getDocumentoProfesor().length > 0)")
    @Mapping(target = "tarea", source = "tarea") // ✅ EXPLÍCITO: mapear tarea completa
    @Mapping(target = "alumno", source = "alumno")
    EntregaResponseDTO toEntregaResponseDTO(EntregaEntity entrega);

    @Mapping(target = "tareaId", source = "tarea.id")
    @Mapping(target = "alumnoId", source = "alumno.id")
    @Mapping(target = "nombreAlumno", source = "alumno.nombre")
    @Mapping(target = "apellidoAlumno", source = "alumno.apellido")
    EntregaSimpleDTO toEntregaSimpleDTO(EntregaEntity entrega);

    List<EntregaResponseDTO> toEntregaResponseDTOList(List<EntregaEntity> entregas);

    List<EntregaSimpleDTO> toEntregaSimpleDTOList(List<EntregaEntity> entregas);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tarea", ignore = true)  // 🔥 CRÍTICO
    @Mapping(target = "alumno", ignore = true) // 🔥 CRÍTICO
    @Mapping(target = "documento", ignore = true)
    @Mapping(target = "nombreDocumento", ignore = true)
    @Mapping(target = "tipoDocumento", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaEntrega", ignore = true)
    @Mapping(target = "nota", ignore = true)
    @Mapping(target = "documentoProfesor", ignore = true)
    @Mapping(target = "nombreDocumentoProfesor", ignore = true)
    @Mapping(target = "tipoDocumentoProfesor", ignore = true)
    EntregaEntity toEntregaEntityWithoutRelations(EntregaCreateDTO dto);

    // Método por defecto para establecer el estado inicial
    default EntregaEntity establecerEstadoInicial(EntregaEntity entrega) {
        entrega.setEstado(EntregaEntity.EstadoEntrega.PENDIENTE);
        return entrega;
    }

}