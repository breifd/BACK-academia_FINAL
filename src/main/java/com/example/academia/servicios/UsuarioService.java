package com.example.academia.servicios;

import com.example.academia.DTOs.LoginResponse;
import com.example.academia.DTOs.Response.UsuarioResponseDTO;
import com.example.academia.DTOs.UsuarioDTO;
import com.example.academia.DTOs.Created.UsuarioCreateDTO;

import java.util.List;
import java.util.Optional;

public interface UsuarioService {

    Optional<UsuarioResponseDTO> findByUsername(String username);

    List<UsuarioResponseDTO> findAll();

    LoginResponse login(String username, String password);

    UsuarioResponseDTO saveUsuario(UsuarioDTO usuario);

    UsuarioResponseDTO createUsuario(UsuarioCreateDTO usuarioDTO);

    UsuarioResponseDTO updateUsuario(Long id, UsuarioDTO usuarioDTO);

    void deleteUsuario(Long id);

    List<UsuarioResponseDTO> findByRol(String rol);

    Optional<UsuarioResponseDTO> findByProfesorId(Long profesorId);

    Optional<UsuarioResponseDTO> findByAlumnoId(Long alumnoId);

    UsuarioResponseDTO syncNameWithRelatedEntity(Long usuarioId);
}