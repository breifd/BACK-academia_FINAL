package com.example.academia.mappers;

import com.example.academia.DTOs.*;
import com.example.academia.DTOs.Created.ProfesorCreateDTO;
import com.example.academia.DTOs.Response.ProfesorResponseDTO;
import com.example.academia.DTOs.SimpleDTO.ProfesorSimpleDTO;
import com.example.academia.entidades.ProfesorEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProfesorMapper {

    ProfesorResponseDTO toProfesorResponseDTO(ProfesorEntity profesor);

    @Mapping(target = "id", ignore = true)
    ProfesorEntity toProfesorEntity(ProfesorCreateDTO dto);

    ProfesorSimpleDTO toProfesorSimpleDTO(ProfesorEntity profesor);
}
