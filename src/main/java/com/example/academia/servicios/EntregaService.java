package com.example.academia.servicios;

import com.example.academia.DTOs.CalificacionDTO;
import com.example.academia.DTOs.DocumentoDTO;
import com.example.academia.DTOs.Created.EntregaCreateDTO;
import com.example.academia.DTOs.Response.EntregaResponseDTO;
import com.example.academia.DTOs.SimpleDTO.EntregaSimpleDTO;
import com.example.academia.entidades.AlumnoEntity;
import com.example.academia.entidades.EntregaEntity;
import com.example.academia.entidades.TareaEntity;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface EntregaService {
    // Obtener todas las entregas (paginadas)
    Page<EntregaResponseDTO> findAll(int page, int size, String sort, String direction);

    // Obtener una entrega por su ID
    Optional<EntregaResponseDTO> findById(Long id);

    Page <EntregaResponseDTO> findEntregasByProfesor (Long profesorId, int page, int size, String sort, String direction);
    // Guardar una entrega
    EntregaResponseDTO saveEntrega(EntregaCreateDTO entrega);

    // Eliminar una entrega
    void deleteEntrega(Long id);

    // -- Operaciones de búsqueda -- //

    // Buscar entregas por tarea
    Page<EntregaResponseDTO> findByTarea(Long tareaId, int page, int size, String sort, String direction);

    // Buscar entregas por alumno
    Page<EntregaResponseDTO> findByAlumno(Long alumnoId, int page, int size, String sort, String direction);

    // Buscar entrega específica por tarea y alumno
    Optional<EntregaResponseDTO> findByTareaAndAlumno(Long tareaId, Long alumnoId);

    // Buscar entregas por estado
    Page<EntregaResponseDTO> findByEstado(String estado, int page, int size, String sort, String direction);

    // Buscar entregas pendientes de calificación para un profesor
    Page<EntregaResponseDTO> findEntregasPendientesCalificacion(Long profesorId, int page, int size, String sort, String direction);

    // -- Operaciones específicas de negocio -- //

    // Crear una nueva entrega
    EntregaResponseDTO crearEntrega(EntregaCreateDTO entregaDTO, Long alumnoId);

    // Calificar una entrega
    EntregaResponseDTO calificarEntrega(Long entregaId, CalificacionDTO calificacionDTO, Long profesorId);

    // -- Estadísticas -- //

    // Contar entregas pendientes para un profesor
    Long countEntregasPendientesCalificacion(Long profesorId);

    // Obtener nota media de entregas para una tarea
    Double getNotaMediaByTarea(Long tareaId);

    EntregaResponseDTO calificarEntregaConDocumento(Long entregaId, CalificacionDTO calificacionDTO, Long profesorId, MultipartFile documentoProfesor) throws IOException;

    DocumentoDTO downloadDocumentoProfesor(Long entregaId);

    // -- Operaciones de documentos -- //

    Long getProfesorIdFromEntrega(Long entregaId);

    // Subir documento para una entrega
    EntregaResponseDTO uploadDocumento(Long entregaId, MultipartFile file, Long alumnoId) throws IOException;

    // Descargar documento de una entrega
    DocumentoDTO downloadDocumento(Long entregaId);

    EntregaResponseDTO updateEntrega(Long entregaId, EntregaCreateDTO entregaDTO, Long alumnoId);

    void generarEntregasAutomaticasPorVencimiento();

    void generarEntregasVencidaParaTarea(TareaEntity tarea);
    // -- Métodos de validación -- //

    Page<EntregaResponseDTO> findByProfesor(Long profesorId, int page, int size, String sort, String direction);

    // Validar que una entrega pertenece a un alumno
    boolean validarEntregaAlumno(Long entregaId, Long alumnoId);

    // Validar que un profesor puede calificar una entrega
    boolean validarEntregaProfesor(Long entregaId, Long profesorId);
}