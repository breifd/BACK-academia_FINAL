package com.example.academia.servicios.serviciosImpl;

import com.example.academia.DTOs.DocumentoDTO;
import com.example.academia.DTOs.Response.TareaResponseDTO;
import com.example.academia.DTOs.SimpleDTO.TareaSimpleDTO;
import com.example.academia.DTOs.TareaDTO;
import com.example.academia.Exceptions.ValidationException;
import com.example.academia.entidades.AlumnoEntity;
import com.example.academia.entidades.CursoEntity;
import com.example.academia.entidades.ProfesorEntity;
import com.example.academia.entidades.TareaEntity;
import com.example.academia.mappers.DocumentoMapper;
import com.example.academia.mappers.TareaMapper;
import com.example.academia.repositorios.AlumnoRepository;
import com.example.academia.repositorios.CursoRepository;
import com.example.academia.repositorios.ProfesorRepository;
import com.example.academia.repositorios.TareaRepository;
import com.example.academia.servicios.TareaService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TareaServiceImpl implements TareaService {

    private final TareaRepository tareaRepository;
    private final ProfesorRepository profesorRepository;
    private final AlumnoRepository alumnoRepository;
    private final CursoRepository cursoRepository;
    private final TareaMapper tareaMapper;
    private final DocumentoMapper documentoMapper;

    private Pageable createPageable(int page, int size, String sort, String direction) {
        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        if (sort == null || sort.isEmpty()) sort = "id";
        return PageRequest.of(page, size, sortDirection, sort);
    }

    @Override
    public Page<TareaResponseDTO> findAll(int page, int size, String sort, String direction) {
        Pageable pageable = createPageable(page, size, sort, direction);
        return tareaRepository.findAll(pageable).map(tareaMapper::toTareaResponseDTO);
    }

    @Override
    public Optional<TareaResponseDTO> findById(Long id) {
        return tareaRepository.findById(id).map(tareaMapper::toTareaResponseDTO);
    }

    @Override
    public List<TareaSimpleDTO> findAllLista() {
        return tareaRepository.findAll().stream()
                .map(tareaMapper::toTareaSimpleDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<TareaResponseDTO> findByNombre(String nombre, int page, int size, String sort, String direction) {
        Pageable pageable = createPageable(page, size, sort, direction);
        if (nombre != null && !nombre.trim().isEmpty()) {
            return tareaRepository.findByNombreContainingIgnoreCase(nombre, pageable)
                    .map(tareaMapper::toTareaResponseDTO);
        } else {
            return tareaRepository.findAll(pageable).map(tareaMapper::toTareaResponseDTO);
        }
    }

    @Override
    public Page<TareaResponseDTO> findByFechaLimiteAntes(LocalDate fecha, int page, int size, String sort, String direction) {
        Pageable pageable = createPageable(page, size, sort, direction);
        return tareaRepository.findByFechaLimiteBefore(fecha, pageable)
                .map(tareaMapper::toTareaResponseDTO);
    }

    @Override
    public Page<TareaResponseDTO> findByFechaLimiteDespues(LocalDate fecha, int page, int size, String sort, String direction) {
        Pageable pageable = createPageable(page, size, sort, direction);
        return tareaRepository.findByFechaLimiteAfter(fecha, pageable)
                .map(tareaMapper::toTareaResponseDTO);
    }

    @Override
    public TareaResponseDTO saveTarea(TareaDTO tarea) {
        TareaEntity tareaEntity = tareaMapper.toTareaEntity(tarea);
        validarFechas(tareaEntity);

        // Si tiene ID, es una actualización y necesitamos mantener el documento existente
        if (tarea.getId() != null) {
            Optional<TareaEntity> tareaExistente = tareaRepository.findById(tarea.getId());
            if (tareaExistente.isPresent() && tareaExistente.get().getDocumento() != null) {
                tareaEntity.setDocumento(tareaExistente.get().getDocumento());
                tareaEntity.setNombreDocumento(tareaExistente.get().getNombreDocumento());
                tareaEntity.setTipoDocumento(tareaExistente.get().getTipoDocumento());
            }
        }

        TareaEntity savedTarea = tareaRepository.save(tareaEntity);
        return tareaMapper.toTareaResponseDTO(savedTarea);
    }

    private void validarFechas(TareaEntity tarea) {
        if (tarea.getFechaPublicacion() != null && tarea.getFechaLimite() != null) {
            if (tarea.getFechaLimite().isBefore(tarea.getFechaPublicacion())) {
                throw new ValidationException("La fecha límite no puede ser anterior a la fecha de publicación");
            }
        }
    }

    @Override
    @Transactional
    public TareaResponseDTO uploadDocumento(Long tareaId, MultipartFile file) throws IOException {
        TareaEntity tarea = tareaRepository.findById(tareaId)
                .orElseThrow(() -> new ValidationException("Tarea no encontrada con ID: " + tareaId));

        tarea.setDocumento(file.getBytes());
        tarea.setNombreDocumento(file.getOriginalFilename());
        tarea.setTipoDocumento(file.getContentType());

        TareaEntity savedTarea = tareaRepository.save(tarea);
        return tareaMapper.toTareaResponseDTO(savedTarea);
    }

    @Override
    public DocumentoDTO downloadDocumento(Long tareaId) {
        TareaEntity tarea = tareaRepository.findById(tareaId)
                .orElseThrow(() -> new ValidationException("Tarea no encontrada con ID: " + tareaId));

        if (tarea.getDocumento() == null || tarea.getDocumento().length == 0) {
            throw new ValidationException("La tarea no tiene documento");
        }

        return documentoMapper.toDocumentoDTO(
                tarea.getNombreDocumento(),
                tarea.getTipoDocumento(),
                tarea.getDocumento()
        );
    }

    @Override
    public void deleteTarea(Long id) {
        tareaRepository.deleteById(id);
    }

    @Override
    public Page<TareaResponseDTO> findTareasProfesor(Long profesorId, int page, int size, String sort, String direction) {
        Pageable pageable = createPageable(page, size, sort, direction);
        return tareaRepository.findByProfesorId(profesorId, pageable)
                .map(tareaMapper::toTareaResponseDTO);
    }

    @Override
    public Page<TareaResponseDTO> findTareasCurso(Long cursoId, int page, int size, String sort, String direction) {
        Pageable pageable = createPageable(page, size, sort, direction);
        return tareaRepository.findByCursoId(cursoId, pageable)
                .map(tareaMapper::toTareaResponseDTO);
    }

    @Override
    public Page<TareaResponseDTO> findTareasAlumno(Long alumnoId, int page, int size, String sort, String direction) {
        Pageable pageable = createPageable(page, size, sort, direction);
        return tareaRepository.findTareasForAlumno(alumnoId, pageable)
                .map(tareaMapper::toTareaResponseDTO);
    }

    @Override
    public List<TareaResponseDTO> findTareasByCursoForAlumno(Long cursoId, Long alumnoId) {
        return tareaRepository.findTareasByCursoForAlumno(cursoId, alumnoId).stream()
                .map(tareaMapper::toTareaResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TareaResponseDTO createTarea(TareaDTO tareaDTO, Long profesorId) {
        ProfesorEntity profesor = profesorRepository.findById(profesorId)
                .orElseThrow(() -> new ValidationException("Profesor no encontrado con ID: " + profesorId));

        CursoEntity curso = cursoRepository.findById(tareaDTO.getCursoId())
                .orElseThrow(() -> new ValidationException("Curso no encontrado con ID: " + tareaDTO.getCursoId()));

        // Comprobar si el profesor está en el curso
        boolean profesorEnCurso = curso.getProfesores().stream()
                .anyMatch(p -> p.getId().equals(profesorId));

        if (!profesorEnCurso) {
            throw new ValidationException("El profesor no imparte en este curso");
        }

        // Crear la tarea
        TareaEntity tarea = tareaMapper.toTareaEntity(tareaDTO);
        tarea.setProfesor(profesor);
        tarea.setCurso(curso);
        tarea.setFechaPublicacion(tareaDTO.getFechaPublicacion() != null ?
                tareaDTO.getFechaPublicacion() : LocalDate.now());

        validarFechas(tarea);

        // Si la tarea no es para todos, asignar alumnos específicos
        if (Boolean.FALSE.equals(tareaDTO.getParaTodosLosAlumnos()) &&
                tareaDTO.getAlumnosIds() != null && !tareaDTO.getAlumnosIds().isEmpty()) {

            Set<AlumnoEntity> alumnosAsignados = new HashSet<>();
            for (Long alumnoId : tareaDTO.getAlumnosIds()) {
                AlumnoEntity alumno = alumnoRepository.findById(alumnoId)
                        .orElseThrow(() -> new ValidationException("Alumno no encontrado con ID: " + alumnoId));

                if (!validarAlumnoCurso(alumnoId, curso.getId())) {
                    throw new ValidationException("El alumno con ID " + alumnoId + " no está matriculado en el curso");
                }

                alumnosAsignados.add(alumno);
            }

            tarea.setAlumnosAsignados(alumnosAsignados);
        }

        TareaEntity savedTarea = tareaRepository.save(tarea);
        return tareaMapper.toTareaResponseDTO(savedTarea);
    }

    @Override
    public boolean canProfesorAssignTareaToAlumnoInCurso(Long profesorId, Long alumnoId, Long cursoId) {
        return tareaRepository.canProfesorAssignTareaToAlumnoInCurso(profesorId, alumnoId, cursoId);
    }

    @Override
    @Transactional
    public TareaResponseDTO asignarTareaAAlumno(Long tareaId, Long alumnoId) {
        TareaEntity tarea = tareaRepository.findById(tareaId)
                .orElseThrow(() -> new ValidationException("Tarea no encontrada con ID: " + tareaId));

        AlumnoEntity alumno = alumnoRepository.findById(alumnoId)
                .orElseThrow(() -> new ValidationException("Alumno no encontrado con ID: " + alumnoId));

        if (!validarAlumnoCurso(alumnoId, tarea.getCurso().getId())) {
            throw new ValidationException("El alumno no está matriculado en el curso de esta tarea");
        }

        if (Boolean.TRUE.equals(tarea.getParaTodosLosAlumnos())) {
            throw new ValidationException("Esta tarea ya está asignada a todos los alumnos del curso");
        }

        boolean yaAsignado = tarea.getAlumnosAsignados().stream()
                .anyMatch(a -> a.getId().equals(alumnoId));

        if (yaAsignado) {
            throw new ValidationException("El alumno ya tiene asignada esta tarea");
        }

        tarea.getAlumnosAsignados().add(alumno);
        TareaEntity savedTarea = tareaRepository.save(tarea);

        return tareaMapper.toTareaResponseDTO(savedTarea);
    }

    @Override
    @Transactional
    public TareaResponseDTO desasignarTareaDeAlumno(Long tareaId, Long alumnoId) {
        TareaEntity tarea = tareaRepository.findById(tareaId)
                .orElseThrow(() -> new ValidationException("Tarea no encontrada con ID: " + tareaId));

        if (Boolean.TRUE.equals(tarea.getParaTodosLosAlumnos())) {
            throw new ValidationException("No se puede desasignar a un alumno de una tarea asignada a todos");
        }

        boolean estaAsignado = tarea.getAlumnosAsignados().stream()
                .anyMatch(alumno -> alumno.getId().equals(alumnoId));

        if (!estaAsignado) {
            throw new ValidationException("El alumno no tiene asignada esta tarea");
        }

        tarea.setAlumnosAsignados(tarea.getAlumnosAsignados().stream()
                .filter(alumno -> !alumno.getId().equals(alumnoId))
                .collect(Collectors.toSet()));

        TareaEntity savedTarea = tareaRepository.save(tarea);

        return tareaMapper.toTareaResponseDTO(savedTarea);
    }

    @Override
    public boolean validarTareaProfesor(Long tareaId, Long profesorId) {
        Optional<TareaEntity> tarea = tareaRepository.findById(tareaId);
        return tarea.isPresent() && tarea.get().getProfesor().getId().equals(profesorId);
    }

    @Override
    public boolean validarAlumnoCurso(Long alumnoId, Long cursoId) {
        Optional<CursoEntity> curso = cursoRepository.findById(cursoId);
        if (curso.isEmpty()) {
            return false;
        }

        return curso.get().getAlumnos().stream()
                .anyMatch(alumno -> alumno.getId().equals(alumnoId));
    }
}