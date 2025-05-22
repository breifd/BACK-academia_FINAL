package com.example.academia.servicios;

import com.example.academia.entidades.AlumnoEntity;
import com.example.academia.entidades.CursoEntity;
import com.example.academia.entidades.UsuarioEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface AlumnoService {

    Page<AlumnoEntity> findAll(int page, int size, String sort , String direction);

    Optional<AlumnoEntity> findById(Long id);

    Page<AlumnoEntity> findByNombreOrApellido(String texto, String apellido, int page, int size, String sort , String direction);

    AlumnoEntity saveAlumno(AlumnoEntity alumno);

    //Realizamos un metodo para el update paracambiar tambien el usuario con el que esta relacionado
    AlumnoEntity updateAlumno(Long id, AlumnoEntity alumno, boolean syncUsuario);
    //
    AlumnoEntity createAlumnoWithUser(AlumnoEntity alumno, UsuarioEntity usuario);

    Page<CursoEntity> getCursosByAlumno(Long alumnoId, int page, int size, String sort , String direction);

    void deleteAlumno(Long id);
}
