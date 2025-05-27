package com.example.academia.controller;

import com.example.academia.DTOs.Created.AlumnoCreateDTO;
import com.example.academia.DTOs.Response.AlumnoResponseDTO;
import com.example.academia.DTOs.SimpleDTO.CursoSimpleDTO;
import com.example.academia.Exceptions.ValidationException;
import com.example.academia.servicios.AlumnoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/alumnos")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class AlumnoController {

    private final AlumnoService alumnoService;

    @PostMapping
    public ResponseEntity<?> createAlumno(@RequestBody AlumnoCreateDTO alumno) {
        try {
            AlumnoResponseDTO createdAlumno = alumnoService.saveAlumno(alumno);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAlumno);
        } catch (ValidationException e) {
            System.err.println("❌ Error de validación: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ Error interno: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno del servidor", "details", e.getMessage()));
        }
    }
    // ELIMINADO: Ya no necesitamos el endpoint separado para crear con usuario
    // porque ahora siempre se crea con usuario

    @GetMapping
    public ResponseEntity<Page<AlumnoResponseDTO>> getAllAlumnos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {
        return ResponseEntity.ok(alumnoService.findAll(page, size, sort, direction));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlumnoResponseDTO> getAlumnoById(@PathVariable long id) {
        return alumnoService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar")
    public ResponseEntity<Page<AlumnoResponseDTO>> buscarAlumnoPorNombreOApellido(
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {
        return ResponseEntity.ok(alumnoService.findByNombreOrApellido(nombre, apellido, page, size, sort, direction));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAlumno(
            @PathVariable long id,
            @RequestBody AlumnoCreateDTO alumno,
            @RequestParam(defaultValue = "false") boolean syncUsuario) {
        try {
            AlumnoResponseDTO updatedAlumno = alumnoService.updateAlumno(id, alumno, syncUsuario);
            return ResponseEntity.ok(updatedAlumno);
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno del servidor: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/cursos")
    public ResponseEntity<Page<CursoSimpleDTO>> getCursosByAlumno(
            @PathVariable long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {
        return ResponseEntity.ok(alumnoService.getCursosByAlumno(id, page, size, sort, direction));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAlumno(@PathVariable long id) {
        try {
            if (alumnoService.findById(id).isPresent()) {
                alumnoService.deleteAlumno(id);
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno del servidor: " + e.getMessage()));
        }
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(ValidationException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}