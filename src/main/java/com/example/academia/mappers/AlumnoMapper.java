package com.example.academia.mappers;

import com.example.academia.DTOs.*;
import com.example.academia.DTOs.Created.AlumnoCreateDTO;
import com.example.academia.DTOs.Response.AlumnoResponseDTO;
import com.example.academia.DTOs.SimpleDTO.AlumnoSimpleDTO;
import com.example.academia.entidades.AlumnoEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AlumnoMapper {


    AlumnoResponseDTO toAlumnoResponseDTO(AlumnoEntity alumno);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuario", ignore = true)  // ✅ IGNORAR relación usuario
    @Mapping(target = "cursos", ignore = true)   // ✅ IGNORAR relación cursos
    @Mapping(target = "tareasAsignadas", ignore = true)  // ✅ IGNORAR relación tareas
    @Mapping(target = "entregas", ignore = true) // ✅ IGNORAR relación entregas
    AlumnoEntity toAlumnoEntity(AlumnoCreateDTO dto);

    AlumnoSimpleDTO toAlumnoSimpleDTO(AlumnoEntity alumno);

    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "cursos", ignore = true)
    @Mapping(target = "tareasAsignadas", ignore = true)
    @Mapping(target = "entregas", ignore = true)
    void updateAlumnoFromDTO(AlumnoResponseDTO dto, @MappingTarget AlumnoEntity alumno);

    List<AlumnoResponseDTO> toAlumnoResponseDTOList(List<AlumnoEntity> alumnos);
    List<AlumnoSimpleDTO> toAlumnoSimpleDTOList(List<AlumnoEntity> alumnos);
}