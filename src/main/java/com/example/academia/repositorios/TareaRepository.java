package com.example.academia.repositorios;

import com.example.academia.entidades.TareaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface TareaRepository extends JpaRepository<TareaEntity, Long> {

    Page<TareaEntity> findAll(Pageable pageable);

    Page<TareaEntity> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);

    Page<TareaEntity> findByFechaLimiteBefore(LocalDate fecha, Pageable pageable);

    Page<TareaEntity> findByFechaLimiteAfter(LocalDate fecha, Pageable pageable);
}