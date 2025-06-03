package com.example.academia.repositorios;

import com.example.academia.entidades.EntregaEntity;
import com.example.academia.entidades.TareaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EntregaRepository extends JpaRepository<EntregaEntity, Long> {

    // Buscar todas las entregas de un alumno
    Page<EntregaEntity> findByAlumnoId(Long alumnoId, Pageable pageable);

    // Buscar todas las entregas de una tarea
    Page<EntregaEntity> findByTareaId(Long tareaId, Pageable pageable);

    // Buscar la entrega de un alumno para una tarea específica
    Optional<EntregaEntity> findByTareaIdAndAlumnoId(Long tareaId, Long alumnoId);

    @Query("SELECT e FROM EntregaEntity e WHERE e.tarea.profesor.id = :profesorId")
    Page<EntregaEntity> findByProfesorId(@Param("profesorId") Long profesorId, Pageable pageable);

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

    @Query("SELECT DISTINCT e FROM EntregaEntity e " +
            "LEFT JOIN FETCH e.tarea t " +
            "LEFT JOIN FETCH t.profesor p " +
            "LEFT JOIN FETCH t.curso c " +
            "LEFT JOIN FETCH e.alumno a " +
            "WHERE p.id = :profesorId " +
            "ORDER BY e.fechaEntrega DESC")
    Page<EntregaEntity> findEntregasByProfesorWithRelations(@Param("profesorId") Long profesorId, Pageable pageable);

    // ✅ CONSULTA OPTIMIZADA: Cargar todas las entregas con relaciones
    @Query("SELECT DISTINCT e FROM EntregaEntity e " +
            "LEFT JOIN FETCH e.tarea t " +
            "LEFT JOIN FETCH t.profesor p " +
            "LEFT JOIN FETCH t.curso c " +
            "LEFT JOIN FETCH e.alumno a " +
            "ORDER BY e.fechaEntrega DESC")
    Page<EntregaEntity> findAllWithRelations(Pageable pageable);

    // ✅ CONSULTA OPTIMIZADA: Cargar entregas de una tarea con relaciones
    @Query("SELECT DISTINCT e FROM EntregaEntity e " +
            "LEFT JOIN FETCH e.tarea t " +
            "LEFT JOIN FETCH t.profesor p " +
            "LEFT JOIN FETCH t.curso c " +
            "LEFT JOIN FETCH e.alumno a " +
            "WHERE t.id = :tareaId " +
            "ORDER BY e.fechaEntrega DESC")
    Page<EntregaEntity> findByTareaIdWithRelations(@Param("tareaId") Long tareaId, Pageable pageable);
}


