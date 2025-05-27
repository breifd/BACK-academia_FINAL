package com.example.academia.controller;

import com.example.academia.DTOs.CalificacionDTO;
import com.example.academia.DTOs.DocumentoDTO;
import com.example.academia.DTOs.Created.EntregaCreateDTO;
import com.example.academia.DTOs.Response.EntregaResponseDTO;
import com.example.academia.DTOs.Response.UsuarioResponseDTO;
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
            // ✅ SIMPLE: Solo usar el alumnoId que viene en el DTO
            Long alumnoId = entregaDTO.getAlumnoId();

            if (alumnoId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Se requiere el ID del alumno"));
            }

            EntregaResponseDTO entrega = entregaService.crearEntrega(entregaDTO, alumnoId);
            return ResponseEntity.status(HttpStatus.CREATED).body(entrega);

        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/documento")
    public ResponseEntity<?> uploadDocumento(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {

        try {
            // ===== MODO DESARROLLO: VALIDACIONES COMENTADAS =====
            Long alumnoId = 1L; // Valor por defecto para testing

            EntregaResponseDTO entrega = entregaService.uploadDocumento(id, file, alumnoId);
            return ResponseEntity.ok(entrega);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al procesar el archivo"));
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/calificar")
    public ResponseEntity<?> calificarEntrega(
            @PathVariable Long id,
            @RequestBody CalificacionDTO calificacionDTO) {

        try {
            // ===== MODO DESARROLLO: VALIDACIONES COMENTADAS =====
            Long profesorId = 1L; // Valor por defecto para testing

            EntregaResponseDTO entrega = entregaService.calificarEntrega(id, calificacionDTO, profesorId);
            return ResponseEntity.ok(entrega);

        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/documento")
    public ResponseEntity<?> downloadDocumento(@PathVariable Long id) {
        try {
            // ===== MODO DESARROLLO: VALIDACIONES COMENTADAS =====
            // Permitir descarga a cualquiera

            DocumentoDTO documento = entregaService.downloadDocumento(id);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(documento.getTipoArchivo()));
            headers.setContentDispositionFormData("attachment", documento.getNombreArchivo());

            return new ResponseEntity<>(documento.getContenido(), headers, HttpStatus.OK);

        } catch (ValidationException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEntrega(@PathVariable Long id) {
        try {
            // ===== MODO DESARROLLO: VALIDACIONES COMENTADAS =====
            entregaService.deleteEntrega(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ===== ENDPOINTS ESPECÍFICOS - MODO DESARROLLO =====

    @GetMapping("/pendientes")
    public ResponseEntity<?> getEntregasPendientesCalificacion(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        try {
            // ===== MODO DESARROLLO: SIN VALIDACIONES =====
            // Devolver todas las entregas pendientes sin restricciones
            Page<EntregaResponseDTO> entregas = entregaService.findAll(page, size, sort, direction);
            return ResponseEntity.ok(entregas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/pendientes/count")
    public ResponseEntity<?> countEntregasPendientesCalificacion() {
        try {
            // ===== MODO DESARROLLO: SIN VALIDACIONES =====
            // Devolver un conteo dummy
            return ResponseEntity.ok(Map.of("pendientes", 0L));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/alumno")
    public ResponseEntity<?> getEntregasByAlumno(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        try {
            // ===== MODO DESARROLLO: SIN VALIDACIONES =====
            // Devolver todas las entregas
            Page<EntregaResponseDTO> entregas = entregaService.findAll(page, size, sort, direction);
            return ResponseEntity.ok(entregas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
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
            // ===== MODO DESARROLLO: SIN VALIDACIONES =====
            Page<EntregaResponseDTO> entregas = entregaService.findByTarea(tareaId, page, size, sort, direction);
            return ResponseEntity.ok(entregas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(ValidationException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}