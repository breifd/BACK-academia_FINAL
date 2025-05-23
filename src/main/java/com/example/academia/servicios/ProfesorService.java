package com.example.academia.servicios;

import com.example.academia.DTOs.Created.ProfesorCreateDTO;
import com.example.academia.DTOs.Created.UsuarioCreateDTO;
import com.example.academia.DTOs.Response.ProfesorResponseDTO;
import com.example.academia.DTOs.SimpleDTO.CursoSimpleDTO;
import com.example.academia.entidades.CursoEntity;
import com.example.academia.entidades.UsuarioEntity;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface ProfesorService {

    Page<ProfesorResponseDTO> findAll(int page, int size, String sort, String direction);

    Optional<ProfesorResponseDTO> findById(Long id);

    List<ProfesorResponseDTO> findAllLista();

    Page<ProfesorResponseDTO> findByNombreOrApellido(String nombre, String apellido, int page, int size, String sort, String direction);

    Page<ProfesorResponseDTO> findByEspecialidad(String especialidad, int page, int size, String sort, String direction);

    ProfesorResponseDTO updateProfesor(Long id, ProfesorCreateDTO profesor, boolean syncUsuario);

    ProfesorResponseDTO createProfesorWithUser(ProfesorCreateDTO profesor);

    ProfesorResponseDTO saveProfesor(ProfesorCreateDTO profesor);

    void deleteProfesor(Long id);

    Page<CursoSimpleDTO> getCursosByProfesor(Long profesorId, int page, int size, String sort, String direction);
}
