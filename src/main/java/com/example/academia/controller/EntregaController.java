package com.example.academia.controller;

import com.example.academia.DTOs.CalificacionDTO;
import com.example.academia.DTOs.DocumentoDTO;
import com.example.academia.DTOs.Created.EntregaCreateDTO;
import com.example.academia.DTOs.Response.EntregaResponseDTO;
import com.example.academia.Exceptions.ValidationException;
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

    @GetMapping
    public ResponseEntity<Page<EntregaResponseDTO>> getAllEntregas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        return ResponseEntity.ok(entregaService.findAll(page, size, sort, direction));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntregaResponseDTO> getEntregaById(@PathVariable Long id) {
        return entregaService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createEntrega(@RequestBody EntregaCreateDTO entregaDTO) {
        try {
            // Obtener el usuario autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            Optional<UsuarioEntity> usuarioOpt = usuarioService.findByUsername(username).map(UsuarioEntity.class::cast);
            if (usuarioOpt.isEmpty() || usuarioOpt.get().getRol() != UsuarioEntity.Rol.Alumno || usuarioOpt.get().getAlumno() == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Solo los alumnos pueden crear entregas"));
            }

            Long alumnoId = usuarioOpt.get().getAlumno().getId();
            EntregaResponseDTO entrega = entregaService.crearEntrega(entregaDTO, alumnoId);
            return ResponseEntity.status(HttpStatus.CREATED).body(entrega);
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
            if (usuarioOpt.isEmpty() || usuarioOpt.get().getRol() != UsuarioEntity.Rol.Alumno) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Solo los alumnos pueden subir documentos a entregas"));
            }

            // Verificar que el alumno es el propietario de la entrega
            if (usuarioOpt.get().getAlumno() != null) {
                Long alumnoId = usuarioOpt.get().getAlumno().getId();
                if (!entregaService.validarEntregaAlumno(id, alumnoId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "No es propietario de esta entrega"));
                }

                EntregaResponseDTO entrega = entregaService.uploadDocumento(id, file, alumnoId);
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

    @PostMapping("/{id}/calificar")
    public ResponseEntity<?> calificarEntrega(
            @PathVariable Long id,
            @RequestBody CalificacionDTO calificacionDTO) {

        try {
            // Obtener el usuario autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            Optional<UsuarioEntity> usuarioOpt = usuarioService.findByUsername(username).map(UsuarioEntity.class::cast);
            if (usuarioOpt.isEmpty() || usuarioOpt.get().getRol() != UsuarioEntity.Rol.Profesor || usuarioOpt.get().getProfesor() == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Solo los profesores pueden calificar entregas"));
            }

            Long profesorId = usuarioOpt.get().getProfesor().getId();
            EntregaResponseDTO entrega = entregaService.calificarEntrega(id, calificacionDTO, profesorId);
            return ResponseEntity.ok(entrega);
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/documento")
    public ResponseEntity<?> downloadDocumento(@PathVariable Long id) {
        try {
            // Obtener el usuario autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            Optional<UsuarioEntity> usuarioOpt = usuarioService.findByUsername(username).map(UsuarioEntity.class::cast);
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

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEntrega(@PathVariable Long id) {
        try {
            // Obtener el usuario autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            Optional<UsuarioEntity> usuarioOpt = usuarioService.findByUsername(username).map(UsuarioEntity.class::cast);
            if (usuarioOpt.isEmpty() || usuarioOpt.get().getRol() != UsuarioEntity.Rol.Admin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Solo los administradores pueden eliminar entregas"));
            }

            entregaService.deleteEntrega(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

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

            Optional<UsuarioEntity> usuarioOpt = usuarioService.findByUsername(username).map(UsuarioEntity.class::cast);
            if (usuarioOpt.isEmpty() || usuarioOpt.get().getRol() != UsuarioEntity.Rol.Profesor || usuarioOpt.get().getProfesor() == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Solo los profesores pueden ver esta información"));
            }

            Long profesorId = usuarioOpt.get().getProfesor().getId();
            Page<EntregaResponseDTO> entregas = entregaService.findEntregasPendientesCalificacion(profesorId, page, size, sort, direction);
            return ResponseEntity.ok(entregas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/pendientes/count")
    public ResponseEntity<?> countEntregasPendientesCalificacion() {
        try {
            // Obtener el usuario autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            Optional<UsuarioEntity> usuarioOpt = usuarioService.findByUsername(username).map(UsuarioEntity.class::cast);
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

            Optional<UsuarioEntity> usuarioOpt = usuarioService.findByUsername(username).map(UsuarioEntity.class::cast);
            if (usuarioOpt.isEmpty() || usuarioOpt.get().getRol() != UsuarioEntity.Rol.Alumno || usuarioOpt.get().getAlumno() == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Solo los alumnos pueden ver esta información"));
            }

            Long alumnoId = usuarioOpt.get().getAlumno().getId();
            Page<EntregaResponseDTO> entregas = entregaService.findByAlumno(alumnoId, page, size, sort, direction);
            return ResponseEntity.ok(entregas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/tarea/{tareaId}")
    public ResponseEntity<?> getEntregasByTarea(
            @PathVariable Long tareaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        try {
            // Obtener el usuario autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            Optional<UsuarioEntity> usuarioOpt = usuarioService.findByUsername(username).map(UsuarioEntity.class::cast);
            if (usuarioOpt.isEmpty() || (usuarioOpt.get().getRol() != UsuarioEntity.Rol.Profesor && usuarioOpt.get().getRol() != UsuarioEntity.Rol.Admin)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "No tiene permisos para ver esta información"));
            }

            Page<EntregaResponseDTO> entregas = entregaService.findByTarea(tareaId, page, size, sort, direction);
            return ResponseEntity.ok(entregas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<?> getEntregasByEstado(
            @PathVariable String estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        try {
            Page<EntregaResponseDTO> entregas = entregaService.findByEstado(estado, page, size, sort, direction);
            return ResponseEntity.ok(entregas);
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(ValidationException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}