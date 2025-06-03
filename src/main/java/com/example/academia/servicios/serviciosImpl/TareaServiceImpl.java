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
import org.springframework.data.domain.*;
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
        // Para listas grandes, usar método optimizado
        if (size > 50) {
            List<TareaEntity> tareas = tareaRepository.findAllWithAlumnos();
            List<TareaResponseDTO> tareasDTO = tareas.stream()
                    .map(tareaMapper::toTareaResponseDTO)
                    .collect(Collectors.toList());

            // Crear Page manualmente
            int startIndex = page * size;
            int endIndex = Math.min(startIndex + size, tareasDTO.size());
            List<TareaResponseDTO> pageContent = tareasDTO.subList(startIndex, endIndex);

            return new PageImpl<>(pageContent, PageRequest.of(page, size), tareasDTO.size());
        }

        // Para listas pequeñas, usar paginación normal
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
    @Transactional
    public TareaResponseDTO saveTarea(TareaDTO tarea) {
        System.out.println("🔧 [SAVE TAREA] Guardando tarea ID: " + tarea.getId());

        TareaEntity tareaEntity;

        if (tarea.getId() != null) {
            // ✅ ACTUALIZACIÓN: Cargar la tarea existente
            System.out.println("🔄 [SAVE TAREA] Modo actualización - ID: " + tarea.getId());

            tareaEntity = tareaRepository.findById(tarea.getId())
                    .orElseThrow(() -> new ValidationException("Tarea no encontrada con ID: " + tarea.getId()));

            // Actualizar campos básicos
            tareaEntity.setNombre(tarea.getNombre());
            tareaEntity.setDescripcion(tarea.getDescripcion());
            tareaEntity.setFechaPublicacion(tarea.getFechaPublicacion());
            tareaEntity.setFechaLimite(tarea.getFechaLimite());
            tareaEntity.setParaTodosLosAlumnos(tarea.getParaTodosLosAlumnos());

            // ✅ ACTUALIZAR CURSO si ha cambiado
            if (tarea.getCursoId() != null && !tarea.getCursoId().equals(tareaEntity.getCurso().getId())) {
                CursoEntity nuevoCurso = cursoRepository.findById(tarea.getCursoId())
                        .orElseThrow(() -> new ValidationException("Curso no encontrado con ID: " + tarea.getCursoId()));
                tareaEntity.setCurso(nuevoCurso);
                System.out.println("📚 [SAVE TAREA] Curso actualizado a: " + nuevoCurso.getNombre());
            }

            // ✅ ACTUALIZAR ALUMNOS ASIGNADOS
            if (Boolean.FALSE.equals(tarea.getParaTodosLosAlumnos()) &&
                    tarea.getAlumnosIds() != null && !tarea.getAlumnosIds().isEmpty()) {

                System.out.println("👥 [SAVE TAREA] Actualizando alumnos específicos: " + tarea.getAlumnosIds().size());

                Set<AlumnoEntity> nuevosAlumnos = new HashSet<>();
                for (Long alumnoId : tarea.getAlumnosIds()) {
                    AlumnoEntity alumno = alumnoRepository.findById(alumnoId)
                            .orElseThrow(() -> new ValidationException("Alumno no encontrado con ID: " + alumnoId));
                    nuevosAlumnos.add(alumno);
                }
                tareaEntity.setAlumnosAsignados(nuevosAlumnos);

            } else if (Boolean.TRUE.equals(tarea.getParaTodosLosAlumnos())) {
                // Si es para todos, limpiar asignaciones específicas
                tareaEntity.setAlumnosAsignados(new HashSet<>());
                System.out.println("🌐 [SAVE TAREA] Limpiando asignaciones específicas - ahora es para todos");
            }

            // Mantener documento existente (se actualiza por separado)
            // No tocar documento, nombreDocumento, tipoDocumento

        } else {
            // ✅ CREACIÓN: Nueva tarea
            System.out.println("🆕 [SAVE TAREA] Modo creación");
            tareaEntity = tareaMapper.toTareaEntityWithoutRelations(tarea);

            // Establecer curso
            if (tarea.getCursoId() != null) {
                CursoEntity curso = cursoRepository.findById(tarea.getCursoId())
                        .orElseThrow(() -> new ValidationException("Curso no encontrado con ID: " + tarea.getCursoId()));
                tareaEntity.setCurso(curso);
            }

            // Establecer profesor
            if (tarea.getProfesorId() != null) {
                ProfesorEntity profesor = profesorRepository.findById(tarea.getProfesorId())
                        .orElseThrow(() -> new ValidationException("Profesor no encontrado con ID: " + tarea.getProfesorId()));
                tareaEntity.setProfesor(profesor);
            }
        }

        // Validar fechas
        validarFechas(tareaEntity);

        // Guardar
        TareaEntity savedTarea = tareaRepository.save(tareaEntity);
        System.out.println("✅ [SAVE TAREA] Tarea guardada correctamente - ID: " + savedTarea.getId());

        return tareaMapper.toTareaResponseDTO(savedTarea);
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
        // Para listas grandes, usar método optimizado sin paginación
        if (size > 50) {
            List<TareaEntity> tareas = tareaRepository.findByProfesorIdWithAlumnos(profesorId);
            List<TareaResponseDTO> tareasDTO = tareas.stream()
                    .map(tareaMapper::toTareaResponseDTO)
                    .collect(Collectors.toList());

            // Crear Page manualmente
            int startIndex = page * size;
            int endIndex = Math.min(startIndex + size, tareasDTO.size());
            List<TareaResponseDTO> pageContent = tareasDTO.subList(startIndex, endIndex);

            return new PageImpl<>(pageContent, PageRequest.of(page, size), tareasDTO.size());
        }

        // Para listas pequeñas, usar paginación normal
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
        // Para listas grandes, usar método optimizado
        if (size > 50) {
            List<TareaEntity> tareas = tareaRepository.findTareasForAlumnoWithAlumnos(alumnoId);
            List<TareaResponseDTO> tareasDTO = tareas.stream()
                    .map(tareaMapper::toTareaResponseDTO)
                    .collect(Collectors.toList());

            // Crear Page manualmente
            int startIndex = page * size;
            int endIndex = Math.min(startIndex + size, tareasDTO.size());
            List<TareaResponseDTO> pageContent = tareasDTO.subList(startIndex, endIndex);

            return new PageImpl<>(pageContent, PageRequest.of(page, size), tareasDTO.size());
        }

        // Para listas pequeñas, usar paginación normal
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
        System.out.println("🔍 === INICIO createTarea OPTIMIZADO ===");
        System.out.println("Profesor ID: " + profesorId);
        System.out.println("Para todos: " + tareaDTO.getParaTodosLosAlumnos());
        System.out.println("Alumnos IDs: " + tareaDTO.getAlumnosIds());

        ProfesorEntity profesor = profesorRepository.findById(profesorId)
                .orElseThrow(() -> new ValidationException("Profesor no encontrado con ID: " + profesorId));

        CursoEntity curso = cursoRepository.findById(tareaDTO.getCursoId())
                .orElseThrow(() -> new ValidationException("Curso no encontrado con ID: " + tareaDTO.getCursoId()));

        // ✅ CREAR LA TAREA MANUALMENTE
        TareaEntity tarea = new TareaEntity();
        tarea.setNombre(tareaDTO.getNombre());
        tarea.setDescripcion(tareaDTO.getDescripcion());
        tarea.setFechaPublicacion(tareaDTO.getFechaPublicacion() != null ?
                tareaDTO.getFechaPublicacion() : LocalDate.now());
        tarea.setFechaLimite(tareaDTO.getFechaLimite());
        tarea.setCurso(curso);
        tarea.setProfesor(profesor);
        tarea.setParaTodosLosAlumnos(tareaDTO.getParaTodosLosAlumnos());

        validarFechas(tarea);

        System.out.println("🔍 Tarea configurada - paraTodos: " + tarea.getParaTodosLosAlumnos());

        // Si la tarea no es para todos, asignar alumnos específicos
        if (Boolean.FALSE.equals(tareaDTO.getParaTodosLosAlumnos()) &&
                tareaDTO.getAlumnosIds() != null && !tareaDTO.getAlumnosIds().isEmpty()) {

            System.out.println("🎯 ASIGNACIÓN ESPECÍFICA - Procesando " + tareaDTO.getAlumnosIds().size() + " alumnos");

            Set<AlumnoEntity> alumnosAsignados = new HashSet<>();
            for (Long alumnoId : tareaDTO.getAlumnosIds()) {
                System.out.println("  📌 Procesando alumno ID: " + alumnoId);

                AlumnoEntity alumno = alumnoRepository.findById(alumnoId)
                        .orElseThrow(() -> new ValidationException("Alumno no encontrado con ID: " + alumnoId));

                alumnosAsignados.add(alumno);
                System.out.println("  ✅ Alumno agregado: " + alumno.getNombre() + " " + alumno.getApellido() + " (ID: " + alumno.getId() + ")");
            }

            tarea.setAlumnosAsignados(alumnosAsignados);
            System.out.println("✅ Total alumnos asignados específicamente: " + alumnosAsignados.size());
        } else {
            System.out.println("🌐 ASIGNACIÓN GLOBAL - Para todos los alumnos del curso");
        }

        TareaEntity savedTarea = tareaRepository.save(tarea);
        System.out.println("💾 Tarea guardada con ID: " + savedTarea.getId());

        // ✅ VERIFICACIÓN POST-GUARDADO
        System.out.println("🔍 === VERIFICACIÓN POST-GUARDADO ===");
        System.out.println("ID: " + savedTarea.getId());
        System.out.println("ParaTodos: " + savedTarea.getParaTodosLosAlumnos());
        System.out.println("AlumnosAsignados count: " + (savedTarea.getAlumnosAsignados() != null ? savedTarea.getAlumnosAsignados().size() : 0));

        if (savedTarea.getAlumnosAsignados() != null) {
            for (AlumnoEntity alumno : savedTarea.getAlumnosAsignados()) {
                System.out.println("  - " + alumno.getNombre() + " " + alumno.getApellido() + " (ID: " + alumno.getId() + ")");
            }
        }
        System.out.println("=====================================");

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

        // ===== MODO DESARROLLO: VALIDACIONES COMENTADAS =====
        /*
        if (!validarAlumnoCurso(alumnoId, tarea.getCurso().getId())) {
            throw new ValidationException("El alumno no está matriculado en el curso de esta tarea");
        }
        */

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

    public void debugTareaAsignacion(Long tareaId) {
        TareaEntity tarea = tareaRepository.findById(tareaId)
                .orElseThrow(() -> new ValidationException("Tarea no encontrada"));

        System.out.println("=== DEBUG TAREA ASIGNACIÓN ===");
        System.out.println("Tarea ID: " + tarea.getId());
        System.out.println("Nombre: " + tarea.getNombre());
        System.out.println("Para todos: " + tarea.getParaTodosLosAlumnos());
        System.out.println("Alumnos asignados: " + tarea.getAlumnosAsignados().size());

        if (!tarea.getParaTodosLosAlumnos()) {
            tarea.getAlumnosAsignados().forEach(alumno ->
                    System.out.println("- " + alumno.getNombre() + " " + alumno.getApellido() + " (ID: " + alumno.getId() + ")"));
        }

        System.out.println("Alumnos del curso: " + tarea.getCurso().getAlumnos().size());
        tarea.getCurso().getAlumnos().forEach(alumno ->
                System.out.println("- " + alumno.getNombre() + " " + alumno.getApellido() + " (ID: " + alumno.getId() + ")"));

        System.out.println("===============================");
    }

    @Override
    @Transactional
    public TareaResponseDTO createTareaConDocumento(TareaDTO tareaDTO, Long profesorId, MultipartFile documento) throws IOException {
        // Crear la tarea usando el método existente
        TareaResponseDTO tareaCreada = this.createTarea(tareaDTO, profesorId);

        // Si hay documento, agregarlo a la tarea recién creada
        if (documento != null && !documento.isEmpty()) {
            TareaEntity tarea = tareaRepository.findById(tareaCreada.getId())
                    .orElseThrow(() -> new ValidationException("Tarea no encontrada después de crear"));

            tarea.setDocumento(documento.getBytes());
            tarea.setNombreDocumento(documento.getOriginalFilename());
            tarea.setTipoDocumento(documento.getContentType());

            TareaEntity tareaConDocumento = tareaRepository.save(tarea);
            return tareaMapper.toTareaResponseDTO(tareaConDocumento);
        }

        return tareaCreada;
    }
}