package com.example.academia.mappers;

import com.example.academia.DTOs.*;
import com.example.academia.DTOs.Created.AlumnoCreateDTO;
import com.example.academia.DTOs.Response.AlumnoResponseDTO;
import com.example.academia.DTOs.SimpleDTO.AlumnoSimpleDTO;
import com.example.academia.entidades.AlumnoEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface AlumnoMapper {

    AlumnoResponseDTO toAlumnoResponseDTO(AlumnoEntity alumno);

    @Mapping(target = "id", ignore = true)
    AlumnoEntity toAlumnoEntity(AlumnoCreateDTO dto);

    AlumnoSimpleDTO toAlumnoSimpleDTO(AlumnoEntity alumno);
}