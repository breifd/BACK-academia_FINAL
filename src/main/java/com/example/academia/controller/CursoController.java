package com.example.academia.controller;

import com.example.academia.DTOs.Created.CursoCreateDTO;
import com.example.academia.DTOs.CursoConDetallesDTO;
import com.example.academia.DTOs.Response.AlumnoResponseDTO;
import com.example.academia.DTOs.Response.CursoResponseDTO;
import com.example.academia.DTOs.Response.ProfesorResponseDTO;
import com.example.academia.DTOs.SimpleDTO.CursoSimpleDTO;
import com.example.academia.Exceptions.ValidationException;
import com.example.academia.servicios.CursoService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/cursos")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class CursoController {

    private static final Logger log = LoggerFactory.getLogger(CursoController.class);

    private final CursoService cursoService;

    @GetMapping
    public ResponseEntity<Page<CursoResponseDTO>> getAllCursos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        return ResponseEntity.ok(cursoService.findAll(page, size, sort, direction));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CursoResponseDTO> getCursoById(@PathVariable Long id) {
        return cursoService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/detalles")
    public ResponseEntity<CursoConDetallesDTO> getCursoWithDetails(@PathVariable Long id) {
        return cursoService.findByIdWithDetails(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/listar")
    public ResponseEntity<List<CursoSimpleDTO>> getAllCursos() {
        return ResponseEntity.ok(cursoService.findAllLista());
    }

    @GetMapping("/buscar")
    public ResponseEntity<Page<CursoResponseDTO>> findByNombre(
            @RequestParam String nombre,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        return ResponseEntity.ok(cursoService.findByNombre(nombre, page, size, sort, direction));
    }

    @GetMapping("/nivel/{nivel}")
    public ResponseEntity<Page<CursoResponseDTO>> findByNivel(
            @PathVariable String nivel,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        try {
            return ResponseEntity.ok(cursoService.findByNivel(nivel, page, size, sort, direction));
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    public ResponseEntity<CursoResponseDTO> createCurso(@RequestBody CursoCreateDTO curso) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cursoService.saveCurso(curso));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CursoResponseDTO> updateCurso(@PathVariable Long id, @RequestBody CursoCreateDTO curso) {
        try {
            CursoResponseDTO updatedCurso = cursoService.updateCurso(id, curso);
            return ResponseEntity.ok(updatedCurso);
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCurso(@PathVariable Long id) {
        return cursoService.findById(id)
                .map(curso -> {
                    cursoService.deleteCurso(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{cursoId}/profesores/{profesorId}")
    public ResponseEntity<?> assignProfesorToCurso(@PathVariable Long cursoId, @PathVariable Long profesorId) {
        try {
            CursoResponseDTO curso = cursoService.assignProfesorToCurso(cursoId, profesorId);
            return ResponseEntity.ok(curso);
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{cursoId}/profesores/{profesorId}")
    public ResponseEntity<?> removeProfesorFromCurso(@PathVariable Long cursoId, @PathVariable Long profesorId) {
        try {
            CursoResponseDTO curso = cursoService.removeProfesorFromCurso(cursoId, profesorId);
            return ResponseEntity.ok(curso);
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{cursoId}/profesores")
    public ResponseEntity<Set<ProfesorResponseDTO>> getProfesoresByCurso(@PathVariable Long cursoId) {
        try {
            Set<ProfesorResponseDTO> profesores = cursoService.getProfesoresByCurso(cursoId);
            return ResponseEntity.ok(profesores);
        } catch (ValidationException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error al obtener profesores para el curso ID {}: {}", cursoId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/profesor/{profesorId}")
    public ResponseEntity<Page<CursoResponseDTO>> getCursosByProfesor(
            @PathVariable Long profesorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        return ResponseEntity.ok(cursoService.findCursosByProfesor(profesorId, page, size, sort, direction));
    }

    @PostMapping("/{cursoId}/alumnos/{alumnoId}")
    public ResponseEntity<?> enrollAlumnoInCurso(@PathVariable Long cursoId, @PathVariable Long alumnoId) {
        try {
            CursoResponseDTO curso = cursoService.enrollAlumnoInCurso(cursoId, alumnoId);
            return ResponseEntity.ok(curso);
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{cursoId}/alumnos/{alumnoId}")
    public ResponseEntity<?> unenrollAlumnoFromCurso(@PathVariable Long cursoId, @PathVariable Long alumnoId) {
        try {
            CursoResponseDTO curso = cursoService.unenrollAlumnoFromCurso(cursoId, alumnoId);
            return ResponseEntity.ok(curso);
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{cursoId}/alumnos")
    public ResponseEntity<Set<AlumnoResponseDTO>> getAlumnosByCurso(@PathVariable Long cursoId) {
        try {
            Set<AlumnoResponseDTO> alumnos = cursoService.getAlumnosByCurso(cursoId);
            return ResponseEntity.ok(alumnos);
        } catch (ValidationException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/alumno/{alumnoId}")
    public ResponseEntity<Page<CursoResponseDTO>> getCursosByAlumno(
            @PathVariable Long alumnoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        return ResponseEntity.ok(cursoService.findCursosByAlumno(alumnoId, page, size, sort, direction));
    }

    @GetMapping("/con-plazas-disponibles")
    public ResponseEntity<Page<CursoResponseDTO>> getCursosConPlazasDisponibles(
            @RequestParam(defaultValue = "1") int plazasMinimas,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        return ResponseEntity.ok(cursoService.findCursosConPlazasDisponibles(plazasMinimas, page, size, sort, direction));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(ValidationException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}