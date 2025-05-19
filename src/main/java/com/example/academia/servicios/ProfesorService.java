package com.example.academia.servicios;

import com.example.academia.entidades.ProfesorEntity;
import com.example.academia.entidades.UsuarioEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProfesorService {

    Page<ProfesorEntity> findAll(int page, int size, String sort, String direction);

    Optional<ProfesorEntity> findById(Long id);

    List<ProfesorEntity> findAllLista();

    Page<ProfesorEntity> findByNombreOrApellido(String nombre, String apellido, int page, int size, String sort , String direction);

    Page<ProfesorEntity> findByEspecialidad(String especialidad, int page, int size, String sort , String direction);

    ProfesorEntity updateProfesor(Long id, ProfesorEntity profesor, boolean syncUsuario);

    ProfesorEntity createProfesorWithUser(ProfesorEntity profesor, UsuarioEntity usuario);

    ProfesorEntity saveProfesor(ProfesorEntity profesor);

    void deleteProfesor(Long id);
}