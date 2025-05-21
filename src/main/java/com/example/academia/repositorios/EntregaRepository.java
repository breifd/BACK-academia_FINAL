package com.example.academia.repositorios;

import com.example.academia.entidades.EntregaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EntregaRepository extends JpaRepository<EntregaEntity, Long> {

    // Buscar todas las entregas de un alumno
    Page<EntregaEntity> findByAlumnoId(Long alumnoId, Pageable pageable);

    // Buscar todas las entregas de una tarea
    Page<EntregaEntity> findByTareaId(Long tareaId, Pageable pageable);

    // Buscar la entrega de un alumno para una tarea específica
    Optional<EntregaEntity> findByTareaIdAndAlumnoId(Long tareaId, Long alumnoId);

    // Buscar entregas por estado
    Page<EntregaEntity> findByEstado(EntregaEntity.EstadoEntrega estado, Pageable pageable);

    // Buscar entregas pendientes de calificación para un profesor
    @Query("SELECT e FROM EntregaEntity e WHERE e.tarea.profesor.id = :profesorId AND e.estado = 'ENTREGADA'")
    Page<EntregaEntity> findEntregasPendientesCalificacion(@Param("profesorId") Long profesorId, Pageable pageable);

    // Contar entregas pendientes para un profesor
    @Query("SELECT COUNT(e) FROM EntregaEntity e WHERE e.tarea.profesor.id = :profesorId AND e.estado = 'ENTREGADA'")
    Long countEntregasPendientesCalificacion(@Param("profesorId") Long profesorId);

    // Estadísticas: Nota media de entregas por tarea
    @Query("SELECT AVG(e.nota) FROM EntregaEntity e WHERE e.tarea.id = :tareaId AND e.nota IS NOT NULL")
    Double getNotaMediaByTarea(@Param("tareaId") Long tareaId);
}


