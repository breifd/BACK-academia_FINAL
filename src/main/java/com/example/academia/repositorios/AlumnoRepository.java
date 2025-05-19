package com.example.academia.repositorios;

import com.example.academia.entidades.AlumnoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlumnoRepository extends JpaRepository<AlumnoEntity,Long> {

    Page<AlumnoEntity> findAll(Pageable pageable);

    Page<AlumnoEntity> findByNombreContainingIgnoreCaseAndApellidoContainingIgnoreCase(String nombre, String apellido, Pageable pageable);

    Page<AlumnoEntity>findByNombreContainingIgnoreCase(String nombre, Pageable pageable);

    Page<AlumnoEntity>findByApellidoContainingIgnoreCase(String apellido, Pageable pageable);

}
