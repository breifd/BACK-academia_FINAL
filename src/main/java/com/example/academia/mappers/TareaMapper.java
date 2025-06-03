package com.example.academia.mappers;
import com.example.academia.DTOs.*;
import com.example.academia.DTOs.Response.TareaResponseDTO;
import com.example.academia.DTOs.SimpleDTO.TareaSimpleDTO;
import com.example.academia.entidades.TareaEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {CursoMapper.class, ProfesorMapper.class, AlumnoMapper.class})
public interface TareaMapper {

    @Mapping(target = "tieneDocumento", expression = "java(tarea.getDocumento() != null && tarea.getDocumento().length > 0)")
    @Mapping(target = "totalEntregas", expression = "java(tarea.getEntregas() != null ? tarea.getEntregas().size() : 0)")
    @Mapping(target = "entregasPendientes", expression = "java(tarea.getEntregas() != null ? (int) tarea.getEntregas().stream().filter(e -> e.getEstado() == com.example.academia.entidades.EntregaEntity.EstadoEntrega.ENTREGADA).count() : 0)")
    TareaResponseDTO toTareaResponseDTO(TareaEntity tarea);

    @Mapping(target = "profesor", source = "profesor")
    @Mapping(target = "curso", source = "curso")
    TareaSimpleDTO toTareaSimpleDTO(TareaEntity tarea);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "curso", ignore = true)    // 🔥 CRÍTICO
    @Mapping(target = "profesor", ignore = true) // 🔥 CRÍTICO
    @Mapping(target = "alumnosAsignados", ignore = true) // 🔥 CRÍTICO
    @Mapping(target = "documento", ignore = true)
    @Mapping(target = "nombreDocumento", ignore = true)
    @Mapping(target = "tipoDocumento", ignore = true)
    @Mapping(target = "entregas", ignore = true)
    TareaEntity toTareaEntityWithoutRelations(TareaDTO dto);

    List<TareaResponseDTO> toTareaResponseDTOList(List<TareaEntity> tareas);
    List<TareaSimpleDTO> toTareaSimpleDTOList(List<TareaEntity> tareas);
}