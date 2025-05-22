package com.example.academia.mappers;

import com.example.academia.DTOs.*;
import com.example.academia.DTOs.Response.UsuarioResponseDTO;
import com.example.academia.entidades.UsuarioEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {ProfesorMapper.class, AlumnoMapper.class})
public interface UsuarioMapper {

    UsuarioResponseDTO toUsuarioResponseDTO(UsuarioEntity usuario);
}