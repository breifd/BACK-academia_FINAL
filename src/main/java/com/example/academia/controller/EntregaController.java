package com.example.academia.controller;

import com.example.academia.DTOs.CalificacionDTO;
import com.example.academia.DTOs.DocumentoDTO;
import com.example.academia.DTOs.EntregaRequestDTO;
import com.example.academia.Exceptions.ValidationException;
import com.example.academia.entidades.EntregaEntity;
import com.example.academia.entidades.UsuarioEntity;
import com.example.academia.servicios.EntregaService;
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
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/entregas")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class EntregaController {

    private final EntregaService entregaService;
    private final UsuarioService usuarioService;

    // -- Endpoint para obtener todas las entregas (paginado) --
    @GetMapping
    public ResponseEntity<Page<EntregaEntity>> getAllEntregas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        return ResponseEntity.ok(entregaService.findAll(page, size, sort, direction));
    }

    // -- Endpoint para obtener una entrega por su ID --
    @GetMapping("/{id}")
    public ResponseEntity<EntregaEntity> getEntregaById(@PathVariable Long id) {
        return entregaService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // -- Endpoint para crear una nueva entrega --
    @PostMapping
    public ResponseEntity<?> createEntrega(@RequestBody EntregaRequestDTO entregaDTO) {
        try {
            // Obtener el usuario autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            Optional<UsuarioEntity> usuarioOpt = usuarioService.findByUsername(username);
            if (usuarioOpt.isEmpty() || usuarioOpt.get().getRol() != UsuarioEntity.Rol.Alumno || usuarioOpt.get().getAlumno() == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Solo los alumnos pueden crear entregas"));
            }

            Long alumnoId = usuarioOpt.get().getAlumno().getId();
            EntregaEntity entrega = entregaService.crearEntrega(entregaDTO, alumnoId);
            return ResponseEntity.status(HttpStatus.CREATED).body(entrega);
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // -- Endpoint para subir un documento a una entrega --
    @PostMapping("/{id}/documento")
    public ResponseEntity<?> uploadDocumento(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {

        try {
            // Obtener el usuario autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            Optional<UsuarioEntity> usuarioOpt = usuarioService.findByUsername(username);
            if (usuarioOpt.isEmpty() || usuarioOpt.get().getRol() != UsuarioEntity.Rol.Alumno) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Solo los alumnos pueden subir documentos a entregas"));
            }

            // Verificar que el alumno es el propietario de la entrega
            if (usuarioOpt.get().getAlumno() != null) {
                Long alumnoId = usuarioOpt.get().getAlumno().getId();
                if (!entregaService.validarEntregaAlumno(id, alumnoId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "No es propietario de esta entrega"));
                }

                EntregaEntity entrega = entregaService.uploadDocumento(id, file, alumnoId);
                return ResponseEntity.ok(entrega);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Usuario sin perfil de alumno"));
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error al procesar el archivo"));
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // -- Endpoint para calificar una entrega --
    @PostMapping("/{id}/calificar")
    public ResponseEntity<?> calificarEntrega(
            @PathVariable Long id,
            @RequestBody CalificacionDTO calificacionDTO) {

        try {
            // Obtener el usuario autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            Optional<UsuarioEntity> usuarioOpt = usuarioService.findByUsername(username);
            if (usuarioOpt.isEmpty() || usuarioOpt.get().getRol() != UsuarioEntity.Rol.Profesor || usuarioOpt.get().getProfesor() == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Solo los profesores pueden calificar entregas"));
            }

            Long profesorId = usuarioOpt.get().getProfesor().getId();
            EntregaEntity entrega = entregaService.calificarEntrega(id, calificacionDTO, profesorId);
            return ResponseEntity.ok(entrega);
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // -- Endpoint para descargar el documento de una entrega --
    @GetMapping("/{id}/documento")
    public ResponseEntity<?> downloadDocumento(@PathVariable Long id) {
        try {
            // Obtener el usuario autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            Optional<UsuarioEntity> usuarioOpt = usuarioService.findByUsername(username);
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Usuario no autenticado"));
            }

            // Verificar permisos según el rol
            if (usuarioOpt.get().getRol() == UsuarioEntity.Rol.Alumno && usuarioOpt.get().getAlumno() != null) {
                // Si es alumno, solo puede ver su propia entrega
                if (!entregaService.validarEntregaAlumno(id, usuarioOpt.get().getAlumno().getId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "No tiene permisos para ver esta entrega"));
                }
            } else if (usuarioOpt.get().getRol() == UsuarioEntity.Rol.Profesor && usuarioOpt.get().getProfesor() != null) {
                // Si es profesor, solo puede ver entregas de sus tareas
                if (!entregaService.validarEntregaProfesor(id, usuarioOpt.get().getProfesor().getId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "No tiene permisos para ver esta entrega"));
                }
            } else if (usuarioOpt.get().getRol() != UsuarioEntity.Rol.Admin) {
                // Si no es alumno, profesor ni admin, no tiene permisos
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "No tiene permisos para ver esta entrega"));
            }

            DocumentoDTO documento = entregaService.downloadDocumento(id);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(documento.getTipoArchivo()));
            headers.setContentDispositionFormData("attachment", documento.getNombreArchivo());

            return new ResponseEntity<>(documento.getContenido(), headers, HttpStatus.OK);
        } catch (ValidationException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    // -- Endpoint para eliminar una entrega (solo admin) --
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEntrega(@PathVariable Long id) {
        try {
            // Obtener el usuario autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            Optional<UsuarioEntity> usuarioOpt = usuarioService.findByUsername(username);
            if (usuarioOpt.isEmpty() || usuarioOpt.get().getRol() != UsuarioEntity.Rol.Admin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Solo los administradores pueden eliminar entregas"));
            }

            entregaService.deleteEntrega(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    // -- Endpoints para profesores -- //

    // Obtener entregas pendientes de calificación para un profesor
    @GetMapping("/pendientes")
    public ResponseEntity<?> getEntregasPendientesCalificacion(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        try {
            // Obtener el usuario autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            Optional<UsuarioEntity> usuarioOpt = usuarioService.findByUsername(username);
            if (usuarioOpt.isEmpty() || usuarioOpt.get().getRol() != UsuarioEntity.Rol.Profesor || usuarioOpt.get().getProfesor() == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Solo los profesores pueden ver esta información"));
            }

            Long profesorId = usuarioOpt.get().getProfesor().getId();
            Page<EntregaEntity> entregas = entregaService.findEntregasPendientesCalificacion(profesorId, page, size, sort, direction);
            return ResponseEntity.ok(entregas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    // Obtener número de entregas pendientes de calificación
    @GetMapping("/pendientes/count")
    public ResponseEntity<?> countEntregasPendientesCalificacion() {
        try {
            // Obtener el usuario autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            Optional<UsuarioEntity> usuarioOpt = usuarioService.findByUsername(username);
            if (usuarioOpt.isEmpty() || usuarioOpt.get().getRol() != UsuarioEntity.Rol.Profesor || usuarioOpt.get().getProfesor() == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Solo los profesores pueden ver esta información"));
            }

            Long profesorId = usuarioOpt.get().getProfesor().getId();
            Long count = entregaService.countEntregasPendientesCalificacion(profesorId);
            return ResponseEntity.ok(Map.of("pendientes", count));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    // -- Endpoints para alumnos -- //

    // Obtener entregas de un alumno
    @GetMapping("/alumno")
    public ResponseEntity<?> getEntregasByAlumno(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        try {
            // Obtener el usuario autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            Optional<UsuarioEntity> usuarioOpt = usuarioService.findByUsername(username);
            if (usuarioOpt.isEmpty() || usuarioOpt.get().getRol() != UsuarioEntity.Rol.Alumno || usuarioOpt.get().getAlumno() == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Solo los alumnos pueden ver esta información"));
            }

            Long alumnoId = usuarioOpt.get().getAlumno().getId();
            Page<EntregaEntity> entregas = entregaService.findByAlumno(alumnoId, page, size, sort, direction);
            return ResponseEntity.ok(entregas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    // -- Manejo de excepciones -- //

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(ValidationException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}