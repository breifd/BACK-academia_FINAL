package com.example.academia.mappers;

import com.example.academia.DTOs.*;
import com.example.academia.DTOs.Created.UsuarioCreateDTO;
import com.example.academia.DTOs.Response.UsuarioResponseDTO;
import com.example.academia.entidades.UsuarioEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ProfesorMapper.class, AlumnoMapper.class})
public interface UsuarioMapper {

    UsuarioResponseDTO toUsuarioResponseDTO(UsuarioEntity usuario);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profesor", ignore = true)
    @Mapping(target = "alumno", ignore = true)
    UsuarioEntity toUsuarioEntity(UsuarioDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profesor", ignore = true)
    @Mapping(target = "alumno", ignore = true)
    UsuarioEntity createUsuarioFromDTO(UsuarioCreateDTO dto);

    void updateUsuarioFromDTO(UsuarioDTO dto, @MappingTarget UsuarioEntity usuario);

    List<UsuarioResponseDTO> toUsuarioResponseDTOList(List<UsuarioEntity> usuarios);
}