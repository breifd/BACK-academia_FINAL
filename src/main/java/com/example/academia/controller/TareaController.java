package com.example.academia.controller;

import com.example.academia.DTOs.DocumentoDTO;
import com.example.academia.DTOs.Response.TareaResponseDTO;
import com.example.academia.DTOs.SimpleDTO.TareaSimpleDTO;
import com.example.academia.DTOs.TareaDTO;
import com.example.academia.Exceptions.ValidationException;
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

    @GetMapping
    public ResponseEntity<Page<TareaResponseDTO>> getAllTareas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        return ResponseEntity.ok(tareaService.findAll(page, size, sort, direction));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TareaResponseDTO> getTareaById(@PathVariable Long id) {
        return tareaService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/listar")
    public ResponseEntity<List<TareaSimpleDTO>> getAllTareas() {
        return ResponseEntity.ok(tareaService.findAllLista());
    }

    @GetMapping("/buscar")
    public ResponseEntity<Page<TareaResponseDTO>> findByNombre(
            @RequestParam String nombre,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        return ResponseEntity.ok(tareaService.findByNombre(nombre, page, size, sort, direction));
    }

    @GetMapping("/pendientes")
    public ResponseEntity<Page<TareaResponseDTO>> findPendientes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        return ResponseEntity.ok(tareaService.findByFechaLimiteDespues(LocalDate.now(), page, size, sort, direction));
    }

    @GetMapping("/vencidas")
    public ResponseEntity<Page<TareaResponseDTO>> findVencidas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        return ResponseEntity.ok(tareaService.findByFechaLimiteAntes(LocalDate.now(), page, size, sort, direction));
    }

    @PostMapping
    public ResponseEntity<?> createTarea(@RequestBody TareaDTO tareaDTO) {
        try {
            // Obtener el usuario autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            Optional<UsuarioEntity> usuarioOpt = usuarioService.findByUsername(username).map(UsuarioEntity.class::cast);
            if (usuarioOpt.isEmpty() || usuarioOpt.get().getRol() != UsuarioEntity.Rol.Profesor || usuarioOpt.get().getProfesor() == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Solo los profesores pueden crear tareas"));
            }

            Long profesorId = usuarioOpt.get().getProfesor().getId();
            TareaResponseDTO tarea = tareaService.createTarea(tareaDTO, profesorId);
            return ResponseEntity.status(HttpStatus.CREATED).body(tarea);
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/documento")
    public ResponseEntity<?> uploadDocumento(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {

        try {
            // Obtener el usuario autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            Optional<UsuarioEntity> usuarioOpt = usuarioService.findByUsername(username).map(UsuarioEntity.class::cast);
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

            TareaResponseDTO tarea = tareaService.uploadDocumento(id, file);
            return ResponseEntity.ok(tarea);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error al procesar el archivo"));
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

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

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTarea(@PathVariable Long id, @RequestBody TareaDTO tareaDTO) {
        try {
            // Obtener el usuario autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            Optional<UsuarioEntity> usuarioOpt = usuarioService.findByUsername(username).map(UsuarioEntity.class::cast);
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

            tareaDTO.setId(id);
            TareaResponseDTO tareaActualizada = tareaService.saveTarea(tareaDTO);
            return ResponseEntity.ok(tareaActualizada);
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTarea(@PathVariable Long id) {
        try {
            // Obtener el usuario autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            Optional<UsuarioEntity> usuarioOpt = usuarioService.findByUsername(username).map(UsuarioEntity.class::cast);
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

    @GetMapping("/profesor/{profesorId}")
    public ResponseEntity<Page<TareaResponseDTO>> getTareasByProfesor(
            @PathVariable Long profesorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        return ResponseEntity.ok(tareaService.findTareasProfesor(profesorId, page, size, sort, direction));
    }

    @GetMapping("/curso/{cursoId}")
    public ResponseEntity<Page<TareaResponseDTO>> getTareasByCurso(
            @PathVariable Long cursoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        return ResponseEntity.ok(tareaService.findTareasCurso(cursoId, page, size, sort, direction));
    }

    @GetMapping("/alumno/{alumnoId}")
    public ResponseEntity<Page<TareaResponseDTO>> getTareasByAlumno(
            @PathVariable Long alumnoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        return ResponseEntity.ok(tareaService.findTareasAlumno(alumnoId, page, size, sort, direction));
    }

    @GetMapping("/curso/{cursoId}/alumno/{alumnoId}")
    public ResponseEntity<List<TareaResponseDTO>> getTareasByCursoForAlumno(
            @PathVariable Long cursoId,
            @PathVariable Long alumnoId) {

        return ResponseEntity.ok(tareaService.findTareasByCursoForAlumno(cursoId, alumnoId));
    }

    @PostMapping("/{tareaId}/asignar-alumno/{alumnoId}")
    public ResponseEntity<?> asignarTareaAAlumno(
            @PathVariable Long tareaId,
            @PathVariable Long alumnoId) {
        try {
            // Validación del usuario
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            Optional<UsuarioEntity> usuarioOpt = usuarioService.findByUsername(username).map(UsuarioEntity.class::cast);

            if (usuarioOpt.isEmpty() || usuarioOpt.get().getRol() != UsuarioEntity.Rol.Profesor) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Solo los profesores pueden asignar tareas"));
            }

            TareaResponseDTO tarea = tareaService.asignarTareaAAlumno(tareaId, alumnoId);
            return ResponseEntity.ok(tarea);
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{tareaId}/desasignar-alumno/{alumnoId}")
    public ResponseEntity<?> desasignarTareaDeAlumno(
            @PathVariable Long tareaId,
            @PathVariable Long alumnoId) {
        try {
            // Validación del usuario
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            Optional<UsuarioEntity> usuarioOpt = usuarioService.findByUsername(username).map(UsuarioEntity.class::cast);

            if (usuarioOpt.isEmpty() || usuarioOpt.get().getRol() != UsuarioEntity.Rol.Profesor) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Solo los profesores pueden desasignar tareas"));
            }

            TareaResponseDTO tarea = tareaService.desasignarTareaDeAlumno(tareaId, alumnoId);
            return ResponseEntity.ok(tarea);
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(ValidationException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}