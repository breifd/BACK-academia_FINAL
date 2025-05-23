package com.example.academia.servicios;

import com.example.academia.DTOs.Created.AlumnoCreateDTO;
import com.example.academia.DTOs.Created.UsuarioCreateDTO;
import com.example.academia.DTOs.Response.AlumnoResponseDTO;
import com.example.academia.DTOs.SimpleDTO.CursoSimpleDTO;
import com.example.academia.entidades.AlumnoEntity;
import com.example.academia.entidades.CursoEntity;
import com.example.academia.entidades.UsuarioEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface AlumnoService {

    Page<AlumnoResponseDTO> findAll(int page, int size, String sort , String direction);

    Optional<AlumnoResponseDTO> findById(Long id);

    Page<AlumnoResponseDTO> findByNombreOrApellido(String texto, String apellido, int page, int size, String sort , String direction);

    AlumnoResponseDTO saveAlumno(AlumnoCreateDTO  alumno);

    //Realizamos un metodo para el update paracambiar tambien el usuario con el que esta relacionado
    AlumnoResponseDTO updateAlumno(Long id, AlumnoCreateDTO  alumno, boolean syncUsuario);
    //
    AlumnoResponseDTO createAlumnoWithUser(AlumnoCreateDTO alumno);

    Page<CursoSimpleDTO> getCursosByAlumno(Long alumnoId, int page, int size, String sort , String direction);

    void deleteAlumno(Long id);
}
