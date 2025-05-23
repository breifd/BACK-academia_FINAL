package com.example.academia.mappers;
import com.example.academia.DTOs.*;
import com.example.academia.DTOs.Response.TareaResponseDTO;
import com.example.academia.DTOs.SimpleDTO.TareaSimpleDTO;
import com.example.academia.entidades.TareaEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {CursoMapper.class, ProfesorMapper.class, AlumnoMapper.class})
public interface TareaMapper {

    TareaResponseDTO toTareaResponseDTO(TareaEntity tarea);

    TareaSimpleDTO toTareaSimpleDTO(TareaEntity tarea);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "documento", ignore = true)
    @Mapping(target = "nombreDocumento", ignore = true)
    @Mapping(target = "tipoDocumento", ignore = true)
    @Mapping(target = "entregas", ignore = true)
    TareaEntity toTareaEntity(TareaDTO dto);

    List<TareaResponseDTO> toTareaResponseDTOList(List<TareaEntity> tareas);
    List<TareaSimpleDTO> toTareaSimpleDTOList(List<TareaEntity> tareas);
}