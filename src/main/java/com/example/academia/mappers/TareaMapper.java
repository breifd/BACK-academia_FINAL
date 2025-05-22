package com.example.academia.mappers;
import com.example.academia.DTOs.*;
import com.example.academia.DTOs.Response.TareaResponseDTO;
import com.example.academia.DTOs.SimpleDTO.TareaSimpleDTO;
import com.example.academia.entidades.TareaEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {CursoMapper.class, ProfesorMapper.class, AlumnoMapper.class})
public interface TareaMapper {

    TareaResponseDTO toTareaResponseDTO(TareaEntity tarea);

    TareaSimpleDTO toTareaSimpleDTO(TareaEntity tarea);
}