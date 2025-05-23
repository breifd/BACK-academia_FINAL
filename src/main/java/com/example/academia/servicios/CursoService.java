package com.example.academia.servicios;

import com.example.academia.DTOs.Created.CursoCreateDTO;
import com.example.academia.DTOs.CursoConDetallesDTO;
import com.example.academia.DTOs.Response.AlumnoResponseDTO;
import com.example.academia.DTOs.Response.CursoResponseDTO;
import com.example.academia.DTOs.Response.ProfesorResponseDTO;
import com.example.academia.DTOs.SimpleDTO.CursoSimpleDTO;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CursoService {

    Page<CursoResponseDTO> findAll(int page, int size, String sort, String direction);

    Optional<CursoResponseDTO> findById(Long id);

    Optional<CursoConDetallesDTO> findByIdWithDetails(Long id);

    List<CursoSimpleDTO> findAllLista();

    Page<CursoResponseDTO> findByNombre(String nombre, int page, int size, String sort, String direction);

    Page<CursoResponseDTO> findByNivel(String nivel, int page, int size, String sort, String direction);

    CursoResponseDTO saveCurso(CursoCreateDTO curso);

    CursoResponseDTO updateCurso(Long id, CursoCreateDTO curso);

    void deleteCurso(Long id);

    // === GESTIÓN DE Profesores ===

    CursoResponseDTO assignProfesorToCurso(Long cursoId, Long profesorId);

    CursoResponseDTO removeProfesorFromCurso(Long cursoId, Long profesorId);

    Set<ProfesorResponseDTO> getProfesoresByCurso(Long cursoId);

    Page<CursoResponseDTO> findCursosByProfesor(Long profesorId, int page, int size, String sort, String direction);

    // === GESTIÓN DE alumnos ===

    CursoResponseDTO enrollAlumnoInCurso(Long cursoId, Long alumnoId);

    CursoResponseDTO unenrollAlumnoFromCurso(Long cursoId, Long alumnoId);

    Set<AlumnoResponseDTO> getAlumnosByCurso(Long cursoId);

    Page<CursoResponseDTO> findCursosByAlumno(Long alumnoId, int page, int size, String sort, String direction);

    // Buscar cursos con plazas disponibles
    Page<CursoResponseDTO> findCursosConPlazasDisponibles(int maxAlumnos, int page, int size, String sort, String direction);
}
