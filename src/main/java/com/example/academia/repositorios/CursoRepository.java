package com.example.academia.repositorios;

import com.example.academia.entidades.CursoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CursoRepository extends JpaRepository<CursoEntity, Long> {
    Page<CursoEntity> findAll(Pageable pageable);
    Page<CursoEntity> findCursosByNombreContainingIgnoreCase(String nome, Pageable pageable);
    Page<CursoEntity> findByNivel (CursoEntity.NivelCurso nivelCurso, Pageable pageable);

    //Busquedas por profesor
    @Query("Select c from CursoEntity c Join c.profesores p where p.id= :profesorId")
    Page<CursoEntity> findByProfesoresId(@Param("profesorId") Long profesorId, Pageable pageable);

    //Busqueda alumno
    @Query("SELECT c FROM CursoEntity c JOIN c.alumnos a WHERE a.id = :alumnoId")
    Page<CursoEntity> findByAlumnosId(@Param("alumnoId") Long alumnoId, Pageable pageable);

    // Cursos con menos de X alumnos (plazas disponibles)
    @Query("SELECT c FROM CursoEntity c WHERE SIZE(c.alumnos) < :maxAlumnos")
    Page<CursoEntity> findCursosWithLessAlumnos(@Param("maxAlumnos") int maxAlumnos, Pageable pageable);

    // Cargar curso con todas sus relaciones
    @Query("SELECT c FROM CursoEntity c " +
            "LEFT JOIN FETCH c.profesores " +
            "LEFT JOIN FETCH c.alumnos " +
            "WHERE c.id = :cursoId")
    Optional<CursoEntity> findByIdWithDetails(@Param("cursoId") Long cursoId);

    // Cargar curso solo con profesores
    @Query("SELECT c FROM CursoEntity c " +
            "LEFT JOIN FETCH c.profesores " +
            "WHERE c.id = :cursoId")
    Optional<CursoEntity> findByIdWithProfesores(@Param("cursoId") Long cursoId);

    // Cargar curso solo con alumnos
    @Query("SELECT c FROM CursoEntity c " +
            "LEFT JOIN FETCH c.alumnos " +
            "WHERE c.id = :cursoId")
    Optional<CursoEntity> findByIdWithAlumnos(@Param("cursoId") Long cursoId);

    // Contar cursos por profesor
    @Query("SELECT COUNT(c) FROM CursoEntity c JOIN c.profesores p WHERE p.id = :profesorId")
    Long countCursosByProfesor(@Param("profesorId") Long profesorId);

    // Contar cursos por alumno
    @Query("SELECT COUNT(c) FROM CursoEntity c JOIN c.alumnos a WHERE a.id = :alumnoId")
    Long countCursosByAlumno(@Param("alumnoId") Long alumnoId);

    // Verificar si un profesor está asignado a un curso
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
            "FROM CursoEntity c JOIN c.profesores p " +
            "WHERE c.id = :cursoId AND p.id = :profesorId")
    Boolean isProfesorAssignedToCurso(@Param("cursoId") Long cursoId, @Param("profesorId") Long profesorId);

    // Verificar si un alumno está inscrito en un curso
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
            "FROM CursoEntity c JOIN c.alumnos a " +
            "WHERE c.id = :cursoId AND a.id = :alumnoId")
    Boolean isAlumnoEnrolledInCurso(@Param("cursoId") Long cursoId, @Param("alumnoId") Long alumnoId);

}
