package com.example.academia.controller;

import com.example.academia.Exceptions.ValidationException;
import com.example.academia.entidades.AlumnoEntity;
import com.example.academia.entidades.CursoEntity;
import com.example.academia.entidades.ProfesorEntity;
import com.example.academia.servicios.CursoService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
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
    public ResponseEntity<Page<CursoEntity>> getAllCursos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        return ResponseEntity.ok(cursoService.findAll(page, size, sort, direction));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CursoEntity> getCursoById(@PathVariable Long id) {
        return cursoService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/listar")
    public ResponseEntity<List<CursoEntity>> getAllCursos() {
        return ResponseEntity.ok(cursoService.findAllLista());
    }

    @GetMapping("/buscar")
    public ResponseEntity<Page<CursoEntity>> findByNombre(
            @RequestParam String nombre,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        return ResponseEntity.ok(cursoService.findByNombre(nombre, page, size, sort, direction));
    }

    @GetMapping("/nivel/{nivel}")
    public ResponseEntity<Page<CursoEntity>> findByNivel(
            @PathVariable String nivel,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        try {
            CursoEntity.NivelCurso nivelEnum = CursoEntity.NivelCurso.valueOf(nivel);
            return ResponseEntity.ok(cursoService.findByNivel(nivelEnum, page, size, sort, direction));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    public ResponseEntity<CursoEntity> createCurso(@RequestBody CursoEntity curso) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cursoService.saveCurso(curso));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CursoEntity> updateCurso(@PathVariable Long id, @RequestBody CursoEntity curso) {
        try {
            CursoEntity updated = cursoService.updateCursoBasicInfo(id, curso);
            return ResponseEntity.ok(updated);
        } catch (ValidationException e) {
            return ResponseEntity.notFound().build();
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
            CursoEntity curso = cursoService.assignProfesorToCurso(cursoId, profesorId);
            return ResponseEntity.ok(curso);
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{cursoId}/profesores/{profesorId}")
    public ResponseEntity<?> removeProfesorFromCurso(@PathVariable Long cursoId, @PathVariable Long profesorId) {
        try {
            CursoEntity curso = cursoService.removeProfesorFromCurso(cursoId, profesorId);
            return ResponseEntity.ok(curso);
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{cursoId}/profesores")
    public ResponseEntity<Set<ProfesorEntity>> getProfesoresByCurso(@PathVariable Long cursoId) {
        try {
            Set<ProfesorEntity> profesores = cursoService.getProfesoresByCurso(cursoId);
            return ResponseEntity.ok(profesores);
        } catch (ValidationException e) {
            // Manejo específico para ValidationException (curso no encontrado)
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.emptySet());
        } catch (Exception e) {
            // Loguear el error para depuración
            log.error("Error al obtener profesores para el curso ID {}: {}", cursoId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(Collections.emptySet());
        }
    }

    @GetMapping("/profesor/{profesorId}")
    public ResponseEntity<Page<CursoEntity>> getCursosByProfesor(
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
            CursoEntity curso = cursoService.enrollAlumnoInCurso(cursoId, alumnoId);
            return ResponseEntity.ok(curso);
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{cursoId}/alumnos/{alumnoId}")
    public ResponseEntity<?> unenrollAlumnoFromCurso(@PathVariable Long cursoId, @PathVariable Long alumnoId) {
        try {
            CursoEntity curso = cursoService.unenrollAlumnoFromCurso(cursoId, alumnoId);
            return ResponseEntity.ok(curso);
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{cursoId}/alumnos")
    public ResponseEntity<Set<AlumnoEntity>> getAlumnosByCurso(@PathVariable Long cursoId) {
        try {
            Set<AlumnoEntity> alumnos = cursoService.getAlumnosByCurso(cursoId);
            return ResponseEntity.ok(alumnos);
        } catch (ValidationException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/alumno/{alumnoId}")
    public ResponseEntity<Page<CursoEntity>> getCursosByAlumno(
            @PathVariable Long alumnoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        return ResponseEntity.ok(cursoService.findCursosByAlumno(alumnoId, page, size, sort, direction));
    }

    // === ENDPOINTS DE BÚSQUEDA AVANZADA ===

    @GetMapping("/con-plazas-disponibles")
    public ResponseEntity<Page<CursoEntity>> getCursosConPlazasDisponibles(
            @RequestParam(defaultValue = "1") int plazasMinimas,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        return ResponseEntity.ok(cursoService.findCursosConPlazasDisponibles(plazasMinimas, page, size, sort, direction));
    }

    @GetMapping("/{id}/detalles")
    public ResponseEntity<CursoEntity> getCursoWithDetails(@PathVariable Long id) {
        try {
            CursoEntity curso = cursoService.getCursoWithDetails(id);
            return ResponseEntity.ok(curso);
        } catch (ValidationException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // === HANDLER DE EXCEPCIONES ===

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(ValidationException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

}