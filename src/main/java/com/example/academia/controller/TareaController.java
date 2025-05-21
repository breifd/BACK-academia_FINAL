package com.example.academia.controller;

import com.example.academia.DTOs.DocumentoDTO;
import com.example.academia.DTOs.TareaDTO;
import com.example.academia.Exceptions.ValidationException;
import com.example.academia.entidades.TareaEntity;
import com.example.academia.entidades.UsuarioEntity;
import com.example.academia.servicios.TareaService;
import com.example.academia.servicios.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/tareas")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class TareaController {

    private final TareaService tareaService;
    private final UsuarioService usuarioService;

    // -- Endpoint para obtener todas las tareas (paginado) --
    @GetMapping
    public ResponseEntity<Page<TareaEntity>> getAllTareas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        return ResponseEntity.ok(tareaService.findAll(page, size, sort, direction));
    }

    // -- Endpoint para obtener una tarea por su ID --
    @GetMapping("/{id}")
    public ResponseEntity<TareaEntity> getTareaById(@PathVariable Long id) {
        return tareaService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // -- Endpoint para obtener todas las tareas (sin paginar) --
    @GetMapping("/listar")
    public ResponseEntity<List<TareaEntity>> getAllTareas() {
        return ResponseEntity.ok(tareaService.findAllLista());
    }

    // -- Endpoint para buscar tareas por nombre --
    @GetMapping("/buscar")
    public ResponseEntity<Page<TareaEntity>> findByNombre(
            @RequestParam String nombre,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        return ResponseEntity.ok(tareaService.findByNombre(nombre, page, size, sort, direction));
    }

    // -- Endpoint para buscar tareas pendientes (fecha límite futura) --
    @GetMapping("/pendientes")
    public ResponseEntity<Page<TareaEntity>> findPendientes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        return ResponseEntity.ok(tareaService.findByFechaLimiteDespues(LocalDate.now(), page, size, sort, direction));
    }

    // -- Endpoint para buscar tareas vencidas (fecha límite pasada) --
    @GetMapping("/vencidas")
    public ResponseEntity<Page<TareaEntity>> findVencidas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        return ResponseEntity.ok(tareaService.findByFechaLimiteAntes(LocalDate.now(), page, size, sort, direction));
    }

    // -- Endpoint para crear una nueva tarea --
    @PostMapping
    public ResponseEntity<?> createTarea(@RequestBody TareaDTO tareaDTO) {
        try {
            // Obtener el usuario autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            Optional<UsuarioEntity> usuarioOpt = usuarioService.findByUsername(username);
            if (usuarioOpt.isEmpty() || usuarioOpt.get().getRol() != UsuarioEntity.Rol.Profesor || usuarioOpt.get().getProfesor() == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Solo los profesores pueden crear tareas"));
            }

            Long profesorId = usuarioOpt.get().getProfesor().getId();
            TareaEntity tarea = tareaService.createTarea(tareaDTO, profesorId);
            return ResponseEntity.status(HttpStatus.CREATED).body(tarea);
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // -- Endpoint para subir un documento a una tarea --
    @PostMapping("/{id}/documento")
    public ResponseEntity<?> uploadDocumento(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {

        try {
            // Obtener el usuario autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            Optional<UsuarioEntity> usuarioOpt = usuarioService.findByUsername(username);
            if (usuarioOpt.isEmpty() || usuarioOpt.get().getRol() != UsuarioEntity.Rol.Profesor) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Solo los profesores pueden subir documentos a tareas"));
            }

            // Verificar que el profesor es el propietario de la tarea
            if (usuarioOpt.get().getProfesor() != null) {
                Long profesorId = usuarioOpt.get().getProfesor().getId();
                if (!tareaService.validarTareaProfesor(id, profesorId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "No es propietario de esta tarea"));
                }
            }

            TareaEntity tarea = tareaService.uploadDocumento(id, file);
            return ResponseEntity.ok(tarea);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error al procesar el archivo"));
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // -- Endpoint para descargar el documento de una tarea --
    @GetMapping("/{id}/documento")
    public ResponseEntity<?> downloadDocumento(@PathVariable Long id) {
        try {
            DocumentoDTO documento = tareaService.downloadDocumento(id);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(documento.getTipoArchivo()));
            headers.setContentDispositionFormData("attachment", documento.getNombreArchivo());

            return new ResponseEntity<>(documento.getContenido(), headers, HttpStatus.OK);
        } catch (ValidationException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    // -- Endpoint para actualizar una tarea --
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTarea(@PathVariable Long id, @RequestBody TareaDTO tareaDTO) {
        try {
            // Obtener el usuario autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            Optional<UsuarioEntity> usuarioOpt = usuarioService.findByUsername(username);
            if (usuarioOpt.isEmpty() || usuarioOpt.get().getRol() != UsuarioEntity.Rol.Profesor) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Solo los profesores pueden modificar tareas"));
            }

            // Verificar que el profesor es el propietario de la tarea
            if (usuarioOpt.get().getProfesor() != null) {
                Long profesorId = usuarioOpt.get().getProfesor().getId();
                if (!tareaService.validarTareaProfesor(id, profesorId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "No es propietario de esta tarea"));
                }
            }

            // Obtener la tarea existente
            TareaEntity tareaExistente = tareaService.findById(id)
                    .orElseThrow(() -> new ValidationException("Tarea no encontrada con ID: " + id));

            // Actualizar los campos de la tarea
            tareaExistente.setNombre(tareaDTO.getNombre());
            tareaExistente.setDescripcion(tareaDTO.getDescripcion());
            if (tareaDTO.getFechaPublicacion() != null) {
                tareaExistente.setFechaPublicacion(tareaDTO.getFechaPublicacion());
            }
            tareaExistente.setFechaLimite(tareaDTO.getFechaLimite());

            // Si cambia el curso, verificar que el profesor imparte en el nuevo curso
            if (!tareaDTO.getCursoId().equals(tareaExistente.getCurso().getId())) {
                boolean puedeAsignar = tareaService.canProfesorAssignTareaToAlumnoInCurso(
                        usuarioOpt.get().getProfesor().getId(),
                        null,
                        tareaDTO.getCursoId()
                );

                if (!puedeAsignar) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                            Map.of("error", "No puede asignar tareas a este curso")
                    );
                }
            }

            // Guardar la tarea actualizada
            TareaEntity tareaActualizada = tareaService.saveTarea(tareaExistente);
            return ResponseEntity.ok(tareaActualizada);
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // -- Endpoint para eliminar una tarea --
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTarea(@PathVariable Long id) {
        try {
            // Obtener el usuario autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            Optional<UsuarioEntity> usuarioOpt = usuarioService.findByUsername(username);
            if (usuarioOpt.isEmpty() || usuarioOpt.get().getRol() != UsuarioEntity.Rol.Profesor) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Solo los profesores pueden eliminar tareas"));
            }

            // Verificar que el profesor es el propietario de la tarea
            if (usuarioOpt.get().getProfesor() != null) {
                Long profesorId = usuarioOpt.get().getProfesor().getId();
                if (!tareaService.validarTareaProfesor(id, profesorId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "No es propietario de esta tarea"));
                }
            }

            // Eliminar la tarea
            tareaService.deleteTarea(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    // -- Endpoints para profesores -- //

    // Obtener tareas creadas por un profesor
    @GetMapping("/profesor/{profesorId}")
    public ResponseEntity<Page<TareaEntity>> getTareasByProfesor(
            @PathVariable Long profesorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        return ResponseEntity.ok(tareaService.findTareasProfesor(profesorId, page, size, sort, direction));
    }

    // Obtener tareas asignadas a un curso
    @GetMapping("/curso/{cursoId}")
    public ResponseEntity<Page<TareaEntity>> getTareasByCurso(
            @PathVariable Long cursoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        return ResponseEntity.ok(tareaService.findTareasCurso(cursoId, page, size, sort, direction));
    }

    // -- Endpoints para alumnos -- //

    // Obtener tareas asignadas a un alumno
    @GetMapping("/alumno/{alumnoId}")
    public ResponseEntity<Page<TareaEntity>> getTareasByAlumno(
            @PathVariable Long alumnoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        return ResponseEntity.ok(tareaService.findTareasAlumno(alumnoId, page, size, sort, direction));
    }

    // Obtener tareas de un curso para un alumno
    @GetMapping("/curso/{cursoId}/alumno/{alumnoId}")
    public ResponseEntity<List<TareaEntity>> getTareasByCursoForAlumno(
            @PathVariable Long cursoId,
            @PathVariable Long alumnoId) {

        return ResponseEntity.ok(tareaService.findTareasByCursoForAlumno(cursoId, alumnoId));
    }

    // -- Manejo de excepciones -- //

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(ValidationException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}