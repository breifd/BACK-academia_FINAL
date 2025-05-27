package com.example.academia.controller;

import com.example.academia.DTOs.Created.ProfesorCreateDTO;
import com.example.academia.DTOs.Response.ProfesorResponseDTO;
import com.example.academia.DTOs.SimpleDTO.CursoSimpleDTO;
import com.example.academia.Exceptions.ValidationException;
import com.example.academia.servicios.ProfesorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/profesores")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class ProfesorController {

    private final ProfesorService profesorService;

    @GetMapping
    public ResponseEntity<Page<ProfesorResponseDTO>> getAllProfesores(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        return ResponseEntity.ok(profesorService.findAll(page, size, sort, direction));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfesorResponseDTO> getProfesorById(@PathVariable Long id) {
        return profesorService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar")
    public ResponseEntity<Page<ProfesorResponseDTO>> searchProfesores(
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        return ResponseEntity.ok(profesorService.findByNombreOrApellido(nombre, apellido, page, size, sort, direction));
    }

    @GetMapping("/listar")
    public ResponseEntity<List<ProfesorResponseDTO>> getAllProfesores() {
        return ResponseEntity.ok(profesorService.findAllLista());
    }

    @GetMapping("/especialidad/{especialidad}")
    public ResponseEntity<Page<ProfesorResponseDTO>> getByEspecialidad(
            @PathVariable String especialidad,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        return ResponseEntity.ok(profesorService.findByEspecialidad(especialidad, page, size, sort, direction));
    }

    @GetMapping("/{id}/cursos")
    public ResponseEntity<Page<CursoSimpleDTO>> getCursosByProfesor(
            @PathVariable long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {
        return ResponseEntity.ok(profesorService.getCursosByProfesor(id, page, size, sort, direction));
    }

    @PostMapping
    public ResponseEntity<?> createProfesor(@RequestBody ProfesorCreateDTO profesor) {
        try {
            // CAMBIO: Siempre usar el método que crea con usuario
            ProfesorResponseDTO createdProfesor = profesorService.saveProfesor(profesor);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdProfesor);
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno del servidor: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProfesor(
            @PathVariable Long id,
            @RequestBody ProfesorCreateDTO profesor,
            @RequestParam(defaultValue = "false") boolean syncUsuario) {
        try {
            ProfesorResponseDTO profesorActualizado = profesorService.updateProfesor(id, profesor, syncUsuario);
            return ResponseEntity.ok(profesorActualizado);
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno del servidor: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProfesor(@PathVariable Long id) {
        try {
            if (profesorService.findById(id).isPresent()) {
                profesorService.deleteProfesor(id);
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

    // ELIMINADO: Ya no necesitamos el endpoint separado para crear con usuario
    // porque ahora siempre se crea con usuario

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(ValidationException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}