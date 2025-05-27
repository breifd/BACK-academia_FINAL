package com.example.academia.servicios.serviciosImpl;

import com.example.academia.DTOs.CalificacionDTO;
import com.example.academia.DTOs.DocumentoDTO;
import com.example.academia.DTOs.Created.EntregaCreateDTO;
import com.example.academia.DTOs.Response.EntregaResponseDTO;
import com.example.academia.DTOs.SimpleDTO.EntregaSimpleDTO;
import com.example.academia.Exceptions.ValidationException;
import com.example.academia.entidades.AlumnoEntity;
import com.example.academia.entidades.EntregaEntity;
import com.example.academia.entidades.TareaEntity;
import com.example.academia.mappers.CalificacionMapper;
import com.example.academia.mappers.DocumentoMapper;
import com.example.academia.mappers.EntregaMapper;
import com.example.academia.repositorios.AlumnoRepository;
import com.example.academia.repositorios.EntregaRepository;
import com.example.academia.repositorios.ProfesorRepository;
import com.example.academia.repositorios.TareaRepository;
import com.example.academia.servicios.EntregaService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EntregaServiceImpl implements EntregaService {

    private final EntregaRepository entregaRepository;
    private final TareaRepository tareaRepository;
    private final AlumnoRepository alumnoRepository;
    private final EntregaMapper entregaMapper;
    private final DocumentoMapper documentoMapper;
    private final CalificacionMapper calificacionMapper;

    // Método auxiliar para crear un Pageable
    private Pageable createPageable(int page, int size, String sort, String direction) {
        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        if (sort == null || sort.isEmpty()) sort = "id";
        return PageRequest.of(page, size, sortDirection, sort);
    }

    // -- Implementaciones de métodos CRUD básicos -- //

    @Override
    public Page<EntregaResponseDTO> findAll(int page, int size, String sort, String direction) {
        Pageable pageable = createPageable(page, size, sort, direction);
        return entregaRepository.findAll(pageable).map(entregaMapper::toEntregaResponseDTO);
    }

    @Override
    public Optional<EntregaResponseDTO> findById(Long id) {
        return entregaRepository.findById(id).map(entregaMapper::toEntregaResponseDTO);
    }

    @Override
    public EntregaResponseDTO saveEntrega(EntregaCreateDTO entrega) {
        EntregaEntity entregaEntity = entregaMapper.toEntregaEntityWithoutRelations(entrega);
        EntregaEntity savedEntrega = entregaRepository.save(entregaEntity);
        return entregaMapper.toEntregaResponseDTO(savedEntrega);
    }

    @Override
    public void deleteEntrega(Long id) {
        entregaRepository.deleteById(id);
    }

    // -- Implementaciones de métodos de búsqueda -- //

    @Override
    public Page<EntregaResponseDTO> findByTarea(Long tareaId, int page, int size, String sort, String direction) {
        Pageable pageable = createPageable(page, size, sort, direction);
        return entregaRepository.findByTareaId(tareaId, pageable)
                .map(entregaMapper::toEntregaResponseDTO);
    }

    @Override
    public Page<EntregaResponseDTO> findByAlumno(Long alumnoId, int page, int size, String sort, String direction) {
        Pageable pageable = createPageable(page, size, sort, direction);
        return entregaRepository.findByAlumnoId(alumnoId, pageable)
                .map(entregaMapper::toEntregaResponseDTO);
    }

    @Override
    public Optional<EntregaResponseDTO> findByTareaAndAlumno(Long tareaId, Long alumnoId) {
        return entregaRepository.findByTareaIdAndAlumnoId(tareaId, alumnoId)
                .map(entregaMapper::toEntregaResponseDTO);
    }

    @Override
    public Page<EntregaResponseDTO> findByEstado(String estado, int page, int size, String sort, String direction) {
        Pageable pageable = createPageable(page, size, sort, direction);
        try {
            EntregaEntity.EstadoEntrega estadoEnum = EntregaEntity.EstadoEntrega.valueOf(estado);
            return entregaRepository.findByEstado(estadoEnum, pageable)
                    .map(entregaMapper::toEntregaResponseDTO);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Estado no válido: " + estado);
        }
    }

    @Override
    public Page<EntregaResponseDTO> findEntregasPendientesCalificacion(Long profesorId, int page, int size, String sort, String direction) {
        Pageable pageable = createPageable(page, size, sort, direction);
        return entregaRepository.findEntregasPendientesCalificacion(profesorId, pageable)
                .map(entregaMapper::toEntregaResponseDTO);
    }

    // -- Implementaciones de métodos específicos de negocio -- //

    @Override
    @Transactional
    public EntregaResponseDTO crearEntrega(EntregaCreateDTO entregaDTO, Long alumnoId) {
        // Verificar que la tarea existe
        TareaEntity tarea = tareaRepository.findById(entregaDTO.getTareaId())
                .orElseThrow(() -> new ValidationException("Tarea no encontrada con ID: " + entregaDTO.getTareaId()));

        // Verificar que el alumno existe
        AlumnoEntity alumno = alumnoRepository.findById(alumnoId)
                .orElseThrow(() -> new ValidationException("Alumno no encontrado con ID: " + alumnoId));

        // ✅ SIMPLE: Solo verificar si ya existe una entrega
        Optional<EntregaEntity> entregaExistente = entregaRepository.findByTareaIdAndAlumnoId(tarea.getId(), alumno.getId());
        if (entregaExistente.isPresent()) {
            throw new ValidationException("Ya existe una entrega para esta tarea");
        }

        // ✅ CREAR LA ENTREGA
        EntregaEntity entrega = new EntregaEntity();
        entrega.setTarea(tarea);
        entrega.setAlumno(alumno);
        entrega.setFechaEntrega(LocalDateTime.now());
        entrega.setComentarios(entregaDTO.getComentarios());

        // ✅ LÓGICA DE VENCIMIENTO
        LocalDateTime ahora = LocalDateTime.now();
        if (tarea.getFechaLimite() != null && ahora.toLocalDate().isAfter(tarea.getFechaLimite())) {
            // Tarea vencida - Nota 0 automática
            entrega.setEstado(EntregaEntity.EstadoEntrega.FUERA_PLAZO);
            entrega.setNota(0.0);
            entrega.setComentarios((entregaDTO.getComentarios() != null ? entregaDTO.getComentarios() + "\n" : "")
                    + "ENTREGA FUERA DE PLAZO - Calificación automática: 0");
        } else {
            // Tarea en tiempo
            entrega.setEstado(EntregaEntity.EstadoEntrega.PENDIENTE);
        }

        EntregaEntity savedEntrega = entregaRepository.save(entrega);
        return entregaMapper.toEntregaResponseDTO(savedEntrega);
    }


    private boolean isAlumnoAsignadoATarea(TareaEntity tarea, Long alumnoId) {
        // Si la tarea es para todos los alumnos del curso
        if (Boolean.TRUE.equals(tarea.getParaTodosLosAlumnos())) {
            // Verificar si el alumno está matriculado en el curso
            return tarea.getCurso().getAlumnos().stream()
                    .anyMatch(alumno -> alumno.getId().equals(alumnoId));
        } else {
            // Verificar si el alumno está en la lista de alumnos asignados
            return tarea.getAlumnosAsignados().stream()
                    .anyMatch(alumno -> alumno.getId().equals(alumnoId));
        }
    }

    @Override
    @Transactional
    public EntregaResponseDTO calificarEntrega(Long entregaId, CalificacionDTO calificacionDTO, Long profesorId) {
        // Verificar que la entrega existe
        EntregaEntity entrega = entregaRepository.findById(entregaId)
                .orElseThrow(() -> new ValidationException("Entrega no encontrada con ID: " + entregaId));

        // Verificar que el profesor de la tarea coincide con el que intenta calificar
        if (!validarEntregaProfesor(entregaId, profesorId)) {
            throw new ValidationException("El profesor no está autorizado para calificar esta entrega");
        }

        // Verificar que la entrega tiene documento
        if (entrega.getDocumento() == null || entrega.getDocumento().length == 0) {
            throw new ValidationException("La entrega no tiene documento para calificar");
        }

        // Verificar que la calificación es válida (entre 0 y 10)
        if (calificacionDTO.getNota() < 0 || calificacionDTO.getNota() > 10) {
            throw new ValidationException("La nota debe estar entre 0 y 10");
        }

        // Actualizar la entrega
        calificacionMapper.updateEntregaFromCalificacion(calificacionDTO, entrega);
        entrega.setEstado(EntregaEntity.EstadoEntrega.CALIFICADA);

        EntregaEntity savedEntrega = entregaRepository.save(entrega);
        return entregaMapper.toEntregaResponseDTO(savedEntrega);
    }

    // -- Implementaciones de métodos de estadísticas -- //

    @Override
    public Long countEntregasPendientesCalificacion(Long profesorId) {
        return entregaRepository.countEntregasPendientesCalificacion(profesorId);
    }

    @Override
    public Double getNotaMediaByTarea(Long tareaId) {
        return entregaRepository.getNotaMediaByTarea(tareaId);
    }

    // -- Implementaciones de métodos de documentos -- //

    @Override
    @Transactional
    public EntregaResponseDTO uploadDocumento(Long entregaId, MultipartFile file, Long alumnoId) throws IOException {
        // Verificar que la entrega existe
        EntregaEntity entrega = entregaRepository.findById(entregaId)
                .orElseThrow(() -> new ValidationException("Entrega no encontrada con ID: " + entregaId));

        // Verificar que el alumno es el propietario de la entrega
        if (!validarEntregaAlumno(entregaId, alumnoId)) {
            throw new ValidationException("El alumno no es el propietario de esta entrega");
        }

        // Actualizar el documento
        entrega.setDocumento(file.getBytes());
        entrega.setNombreDocumento(file.getOriginalFilename());
        entrega.setTipoDocumento(file.getContentType());

        // Actualizar fecha de entrega y estado
        entrega.setFechaEntrega(LocalDateTime.now());

        // Verificar si está dentro del plazo
        if (entrega.getTarea().getFechaLimite() != null &&
                entrega.getFechaEntrega().toLocalDate().isAfter(entrega.getTarea().getFechaLimite())) {
            entrega.setEstado(EntregaEntity.EstadoEntrega.FUERA_PLAZO);
        } else {
            entrega.setEstado(EntregaEntity.EstadoEntrega.ENTREGADA);
        }

        EntregaEntity savedEntrega = entregaRepository.save(entrega);
        return entregaMapper.toEntregaResponseDTO(savedEntrega);
    }

    @Override
    public DocumentoDTO downloadDocumento(Long entregaId) {
        EntregaEntity entrega = entregaRepository.findById(entregaId)
                .orElseThrow(() -> new ValidationException("Entrega no encontrada con ID: " + entregaId));

        if (entrega.getDocumento() == null || entrega.getDocumento().length == 0) {
            throw new ValidationException("La entrega no tiene documento");
        }

        return documentoMapper.toDocumentoDTO(
                entrega.getNombreDocumento(),
                entrega.getTipoDocumento(),
                entrega.getDocumento()
        );
    }

    // -- Implementaciones de métodos de validación -- //

    @Override
    public boolean validarEntregaAlumno(Long entregaId, Long alumnoId) {
        Optional<EntregaEntity> entrega = entregaRepository.findById(entregaId);
        return entrega.isPresent() && entrega.get().getAlumno().getId().equals(alumnoId);
    }

    @Override
    public boolean validarEntregaProfesor(Long entregaId, Long profesorId) {
        Optional<EntregaEntity> entrega = entregaRepository.findById(entregaId);
        return entrega.isPresent() && entrega.get().getTarea().getProfesor().getId().equals(profesorId);
    }
}