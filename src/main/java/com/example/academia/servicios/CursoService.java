package com.example.academia.servicios;

import com.example.academia.entidades.AlumnoEntity;
import com.example.academia.entidades.CursoEntity;
import com.example.academia.entidades.ProfesorEntity;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CursoService {

    Page<CursoEntity> findAll(int page, int size, String sort, String direction);

    Optional<CursoEntity> findById(Long id);

    List<CursoEntity> findAllLista();

    CursoEntity updateCursoBasicInfo(Long id, CursoEntity cursoNuevo);

    Page<CursoEntity> findByNombre(String nombre, int page, int size, String sort, String direction);

    Page<CursoEntity> findByNivel(CursoEntity.NivelCurso nivel, int page, int size, String sort, String direction);

    CursoEntity saveCurso(CursoEntity curso);

    void deleteCurso(Long id);

    // === GESTIÓN DE Profesores ===

    CursoEntity assignProfesorToCurso(Long cursoId, Long profesorId);

    CursoEntity removeProfesorFromCurso(Long cursoId, Long profesorId);

    Set<ProfesorEntity> getProfesoresByCurso(Long cursoId);

    Page<CursoEntity> findCursosByProfesor(Long profesorId, int page, int size, String sort, String direction);

    // === GESTIÓN DE alumnos ===

    CursoEntity enrollAlumnoInCurso(Long cursoId, Long alumnoId);

    CursoEntity unenrollAlumnoFromCurso(Long cursoId, Long alumnoId);

    Set<AlumnoEntity> getAlumnosByCurso(Long cursoId);

    Page<CursoEntity> findCursosByAlumno(Long alumnoId, int page, int size, String sort, String direction);

    // Buscar cursos con plazas disponibles (menos de X alumnos)
    Page<CursoEntity> findCursosConPlazasDisponibles(int maxAlumnos, int page, int size, String sort, String direction);

    // Obtener estadísticas de un curso (número de profesores y alumnos)
    CursoEntity getCursoWithDetails(Long cursoId);

}

