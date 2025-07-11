package com.example.academia.repositorios;

import com.example.academia.entidades.TareaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TareaRepository extends JpaRepository<TareaEntity, Long> {


    // ✅ OPTIMIZACIÓN: Cargar tareas con alumnos en una sola consulta
    @Query("SELECT DISTINCT t FROM TareaEntity t " +
            "LEFT JOIN FETCH t.alumnosAsignados " +
            "LEFT JOIN FETCH t.curso " +
            "LEFT JOIN FETCH t.profesor " +
            "WHERE t.profesor.id = :profesorId " +
            "ORDER BY t.fechaLimite")
    List<TareaEntity> findByProfesorIdWithAlumnos(@Param("profesorId") Long profesorId);

    // ✅ OPTIMIZACIÓN: Cargar todas las tareas con relaciones
    @Query("SELECT DISTINCT t FROM TareaEntity t " +
            "LEFT JOIN FETCH t.alumnosAsignados " +
            "LEFT JOIN FETCH t.curso " +
            "LEFT JOIN FETCH t.profesor " +
            "ORDER BY t.fechaLimite")
    List<TareaEntity> findAllWithAlumnos();

    // ✅ OPTIMIZACIÓN: Cargar tareas para alumno con relaciones
    @Query("SELECT DISTINCT t FROM TareaEntity t " +
            "LEFT JOIN FETCH t.alumnosAsignados " +
            "LEFT JOIN FETCH t.curso " +
            "LEFT JOIN FETCH t.profesor " +
            "WHERE " +
            "  (t.paraTodosLosAlumnos = true AND EXISTS (" +
            "    SELECT 1 FROM t.curso.alumnos a WHERE a.id = :alumnoId" +
            "  )) " +
            "  OR " +
            "  (t.paraTodosLosAlumnos = false AND EXISTS (" +
            "    SELECT 1 FROM t.alumnosAsignados aa WHERE aa.id = :alumnoId" +
            "  )) " +
            "ORDER BY t.fechaLimite")
    List<TareaEntity> findTareasForAlumnoWithAlumnos(@Param("alumnoId") Long alumnoId);

    Page<TareaEntity> findAll(Pageable pageable);

    Page<TareaEntity> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);

    Page<TareaEntity> findByFechaLimiteBefore(LocalDate fecha, Pageable pageable);

    Page<TareaEntity> findByFechaLimiteAfter(LocalDate fecha, Pageable pageable);

    Page<TareaEntity> findByCursoId(Long cursoId, Pageable pageable);

    Page<TareaEntity> findByProfesorId(Long profesorId, Pageable pageable);

    Page<TareaEntity> findByCursoIdAndProfesorId(Long cursoId, Long profesorId, Pageable pageable);

    //Seleccionar todas las tareas asignadas al alumno de forma que se monstraran las tareas donde el alumno está en el curso y la tarea es para todos y las suyas propias
    @Query("SELECT DISTINCT t FROM TareaEntity t " +
            "WHERE " +
            "  (t.paraTodosLosAlumnos = true AND EXISTS (" +
            "    SELECT 1 FROM t.curso.alumnos a WHERE a.id = :alumnoId" +
            "  )) " +
            "  OR " +
            "  (t.paraTodosLosAlumnos = false AND EXISTS (" +
            "    SELECT 1 FROM t.alumnosAsignados aa WHERE aa.id = :alumnoId" +
            "  ))")
    Page<TareaEntity> findTareasForAlumno(@Param("alumnoId") Long alumnoId, Pageable pageable);

    // ✅ NUEVO: Buscar tareas vencidas que no tienen entregas de algunos alumnos
    @Query("SELECT DISTINCT t FROM TareaEntity t " +
            "WHERE t.fechaLimite < :fecha " +
            "AND (t.paraTodosLosAlumnos = true OR t.alumnosAsignados IS NOT EMPTY)")
    List<TareaEntity> findTareasVencidasSinEntregas(@Param("fecha") LocalDate fecha);

    //Verificar que el profesor está en el mismo curso que el alumno al que quiere enviarle la tarea, sino es así devuelve false
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM CursoEntity c JOIN c.profesores p JOIN c.alumnos a WHERE p.id = :profesorId AND a.id = :alumnoId AND c.id = :cursoId")
    boolean canProfesorAssignTareaToAlumnoInCurso(@Param("profesorId") Long profesorId, @Param("alumnoId") Long alumnoId, @Param("cursoId") Long cursoId);

    // Obtendremos todas las tareas que tiene un alumno asignadas del curso en cuestión
    @Query("SELECT t FROM TareaEntity t WHERE t.curso.id = :cursoId AND (t.paraTodosLosAlumnos = true OR :alumnoId IN (SELECT a.id FROM t.alumnosAsignados a))")
    List<TareaEntity> findTareasByCursoForAlumno(@Param("cursoId") Long cursoId, @Param("alumnoId") Long alumnoId);

}