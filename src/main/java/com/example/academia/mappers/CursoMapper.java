package com.example.academia.mappers;

import com.example.academia.DTOs.*;
import com.example.academia.DTOs.Created.CursoCreateDTO;
import com.example.academia.DTOs.Response.CursoResponseDTO;
import com.example.academia.DTOs.SimpleDTO.CursoSimpleDTO;
import com.example.academia.entidades.CursoEntity;
import org.mapstruct.*;
import java.util.List;

@Mapper(componentModel = "spring", uses = {ProfesorMapper.class, AlumnoMapper.class})
public interface CursoMapper {

    CursoResponseDTO toCursoResponseDTO(CursoEntity curso);

    @Mapping(target = "id", ignore = true)
    CursoEntity toCursoEntity(CursoCreateDTO dto);

    CursoConDetallesDTO toCursoConDetallesDTO(CursoEntity curso);

    CursoSimpleDTO toCursoSimpleDTO(CursoEntity curso);

    List<CursoResponseDTO> toCursoResponseDTOList(List<CursoEntity> cursos);
}