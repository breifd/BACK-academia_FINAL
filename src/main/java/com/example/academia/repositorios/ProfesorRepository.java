package com.example.academia.repositorios;

import com.example.academia.entidades.AlumnoEntity;
import com.example.academia.entidades.ProfesorEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ProfesorRepository extends JpaRepository<ProfesorEntity, Long> {
    Page<ProfesorEntity> findAll(Pageable pageable);
    List<ProfesorEntity> findAll();
    Page<ProfesorEntity> findByNombreContainingIgnoreCaseAndApellidoContainingIgnoreCase(String nombre, String apellido, Pageable pageable);
    Page<ProfesorEntity>findByNombreContainingIgnoreCase(String nombre, Pageable pageable);
    Page<ProfesorEntity> findByApellidoContainingIgnoreCase(String apellido, Pageable pageable);
    Page<ProfesorEntity> findByEspecialidadIgnoreCase(String especialidad, Pageable pageable);

}
