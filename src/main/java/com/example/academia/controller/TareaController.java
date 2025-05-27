package com.example.academia.controller;

import com.example.academia.DTOs.DocumentoDTO;
import com.example.academia.DTOs.Response.TareaResponseDTO;
import com.example.academia.DTOs.Response.UsuarioResponseDTO;
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
            // ✅ SOLUCIÓN: Usar profesorId del DTO si está presente, sino usar valor por defecto
            Long profesorId = tareaDTO.getProfesorId() != null ? tareaDTO.getProfesorId() : 1L;

            System.out.println("🔧 [DEBUG] Creando tarea con profesorId: " + profesorId);
            System.out.println("🔧 [DEBUG] ProfessorId del DTO: " + tareaDTO.getProfesorId());

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
            // ===== MODO DESARROLLO: VALIDACIONES COMENTADAS =====
            TareaResponseDTO tarea = tareaService.uploadDocumento(id, file);
            return ResponseEntity.ok(tarea);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al procesar el archivo"));
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
            // ===== MODO DESARROLLO: VALIDACIONES COMENTADAS =====
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
            // ===== MODO DESARROLLO: VALIDACIONES COMENTADAS =====
            tareaService.deleteTarea(id);
            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
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
            // ===== MODO DESARROLLO: VALIDACIONES COMENTADAS =====
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
            // ===== MODO DESARROLLO: VALIDACIONES COMENTADAS =====
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