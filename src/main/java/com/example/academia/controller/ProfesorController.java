package com.example.academia.controller;

import com.example.academia.DTOs.Created.ProfesorCreateDTO;
import com.example.academia.DTOs.Created.UsuarioCreateDTO;
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
    public ResponseEntity<ProfesorResponseDTO> createProfesor(@RequestBody ProfesorCreateDTO profesor) {
        return ResponseEntity.status(HttpStatus.CREATED).body(profesorService.saveProfesor(profesor));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProfesorResponseDTO> updateProfesor(
            @PathVariable Long id,
            @RequestBody ProfesorCreateDTO profesor,
            @RequestParam(defaultValue = "false") boolean syncUsuario) {
        try {
            ProfesorResponseDTO profesorActualizado = profesorService.updateProfesor(id, profesor, syncUsuario);
            return ResponseEntity.ok(profesorActualizado);
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProfesor(@PathVariable Long id) {
        return profesorService.findById(id)
                .map(profesor -> {
                    profesorService.deleteProfesor(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/con-usuario")
    public ResponseEntity<?> crearWithUser(@RequestBody ProfesorCreateDTO profesorDTO) {
        try {
            ProfesorResponseDTO createdProfesor = profesorService.createProfesorWithUser(profesorDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdProfesor);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(ValidationException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}