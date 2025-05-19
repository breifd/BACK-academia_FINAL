package com.example.academia.repositorios;

import com.example.academia.entidades.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {

    Optional<UsuarioEntity> findByUsername(String username);

    Boolean existsByUsername(String username);

    List<UsuarioEntity> findByRol(UsuarioEntity.Rol rol);

    Optional<UsuarioEntity> findByProfesorId(Long profesorId);

    Optional<UsuarioEntity> findByAlumnoId(Long alumnoId);
}