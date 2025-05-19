package com.example.academia.servicios;

import com.example.academia.DTOs.LoginResponse;
import com.example.academia.DTOs.UsuarioDTO;
import com.example.academia.entidades.UsuarioEntity;

import java.util.List;
import java.util.Optional;


public interface UsuarioService {


    Optional<UsuarioEntity> findByUsername(String username);

    List<UsuarioEntity> findAll();

    LoginResponse login(String username, String password);

    UsuarioEntity saveUsuario(UsuarioEntity usuario);

    UsuarioEntity createUsuario(UsuarioDTO usuarioDTO);

    UsuarioEntity updateUsuario(Long id, UsuarioDTO usuarioDTO);

    void deleteUsuario(Long id);

    List<UsuarioEntity> findByRol(UsuarioEntity.Rol rol);

    Optional<UsuarioEntity> findByProfesorId(Long profesorId);

    Optional<UsuarioEntity> findByAlumnoId(Long alumnoId);
    //Intento sincronizar los nombres del usuario con los de la entidad relacionada (profesor o alumno)
    //No vale de nada que el usuario se llame Antonio pero luego en la tabla
    UsuarioEntity syncNameWithRelatedEntity(Long usuarioId);
}
