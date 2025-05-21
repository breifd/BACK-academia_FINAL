package com.example.academia.servicios;
import com.example.academia.DTOs.DocumentoDTO;
import com.example.academia.DTOs.TareaDTO;
import com.example.academia.entidades.TareaEntity;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TareaService {


    Page<TareaEntity> findAll(int page, int size, String sort, String direction);

    Optional<TareaEntity> findById(Long id);

    List<TareaEntity> findAllLista();

    Page<TareaEntity> findByNombre(String nombre, int page, int size, String sort, String direction);

    Page<TareaEntity> findByFechaLimiteAntes(LocalDate fecha, int page, int size, String sort, String direction);

    Page<TareaEntity> findByFechaLimiteDespues(LocalDate fecha, int page, int size, String sort, String direction);

    TareaEntity saveTarea(TareaEntity tarea);

    TareaEntity uploadDocumento(Long tareaId, MultipartFile file) throws IOException; // Manera de Cargar un  archivo como MultipartFile

    DocumentoDTO downloadDocumento(Long tareaId);

    void deleteTarea(Long id);

    // Buscar tareas de un profesor
    Page<TareaEntity> findTareasProfesor(Long profesorId, int page, int size, String sort, String direction);

    // Buscar tareas de un curso
    Page<TareaEntity> findTareasCurso(Long cursoId, int page, int size, String sort, String direction);

    // Buscar tareas para un alumno
    Page<TareaEntity> findTareasAlumno(Long alumnoId, int page, int size, String sort, String direction);

    // Buscar tareas de un curso para un alumno
    List<TareaEntity> findTareasByCursoForAlumno(Long cursoId, Long alumnoId);

    TareaEntity createTarea(TareaDTO tareaDTO, Long profesorId);

    // Verificar si un profesor puede asignar tareas a un alumno en un curso
    boolean canProfesorAssignTareaToAlumnoInCurso(Long profesorId, Long alumnoId, Long cursoId);

    // Asignar una tarea existente a un alumno adicional
    TareaEntity asignarTareaAAlumno(Long tareaId, Long alumnoId);

    // Desasignar una tarea de un alumno
    TareaEntity desasignarTareaDeAlumno(Long tareaId, Long alumnoId);

    // Validar que una tarea pertenece a un profesor
    boolean validarTareaProfesor(Long tareaId, Long profesorId);

    // Validar que un alumno está matriculado en el curso de una tarea
    boolean validarAlumnoCurso(Long alumnoId, Long cursoId);

}