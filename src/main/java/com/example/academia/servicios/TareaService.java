package com.example.academia.servicios;

import com.example.academia.DTOs.DocumentoDTO;
import com.example.academia.DTOs.Response.TareaResponseDTO;
import com.example.academia.DTOs.SimpleDTO.TareaSimpleDTO;
import com.example.academia.DTOs.TareaDTO;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TareaService {

    Page<TareaResponseDTO> findAll(int page, int size, String sort, String direction);

    Optional<TareaResponseDTO> findById(Long id);

    List<TareaSimpleDTO> findAllLista();

    Page<TareaResponseDTO> findByNombre(String nombre, int page, int size, String sort, String direction);

    Page<TareaResponseDTO> findByFechaLimiteAntes(LocalDate fecha, int page, int size, String sort, String direction);

    Page<TareaResponseDTO> findByFechaLimiteDespues(LocalDate fecha, int page, int size, String sort, String direction);

    TareaResponseDTO saveTarea(TareaDTO tarea);

    TareaResponseDTO uploadDocumento(Long tareaId, MultipartFile file) throws IOException;

    DocumentoDTO downloadDocumento(Long tareaId);

    void deleteTarea(Long id);

    // Buscar tareas de un profesor
    Page<TareaResponseDTO> findTareasProfesor(Long profesorId, int page, int size, String sort, String direction);

    // Buscar tareas de un curso
    Page<TareaResponseDTO> findTareasCurso(Long cursoId, int page, int size, String sort, String direction);

    // Buscar tareas para un alumno
    Page<TareaResponseDTO> findTareasAlumno(Long alumnoId, int page, int size, String sort, String direction);

    // Buscar tareas de un curso para un alumno
    List<TareaResponseDTO> findTareasByCursoForAlumno(Long cursoId, Long alumnoId);

    TareaResponseDTO createTarea(TareaDTO tareaDTO, Long profesorId);

    // Verificar si un profesor puede asignar tareas a un alumno en un curso
    boolean canProfesorAssignTareaToAlumnoInCurso(Long profesorId, Long alumnoId, Long cursoId);

    // Asignar una tarea existente a un alumno adicional
    TareaResponseDTO asignarTareaAAlumno(Long tareaId, Long alumnoId);

    // Desasignar una tarea de un alumno
    TareaResponseDTO desasignarTareaDeAlumno(Long tareaId, Long alumnoId);

    // Validar que una tarea pertenece a un profesor
    boolean validarTareaProfesor(Long tareaId, Long profesorId);

    // Validar que un alumno está matriculado en el curso de una tarea
    boolean validarAlumnoCurso(Long alumnoId, Long cursoId);
}