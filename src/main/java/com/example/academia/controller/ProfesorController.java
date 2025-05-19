package com.example.academia.controller;

import com.example.academia.Exceptions.ValidationException;
import com.example.academia.entidades.ProfesorEntity;
import com.example.academia.entidades.UsuarioEntity;
import com.example.academia.repositorios.UsuarioRepository;
import com.example.academia.servicios.ProfesorService;
import com.example.academia.servicios.UsuarioService;
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
    private final UsuarioRepository usuarioRepository;

    @GetMapping
    public ResponseEntity<Page<ProfesorEntity>> getAllProfesores(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        return ResponseEntity.ok(profesorService.findAll(page, size, sort, direction));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfesorEntity> getProfesorById(@PathVariable Long id) {
        return profesorService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar")
    public ResponseEntity<Page<ProfesorEntity>> searchProfesores(
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        return ResponseEntity.ok(profesorService.findByNombreOrApellido(nombre,apellido, page, size, sort, direction));
    }
    @GetMapping("/listar")
    public ResponseEntity<List<ProfesorEntity>> searchProfesores(){
       return ResponseEntity.ok(profesorService.findAllLista());
    }

    @GetMapping("/especialidad/{especialidad}")
    public ResponseEntity<Page<ProfesorEntity>> getByEspecialidad(
            @PathVariable String especialidad,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        return ResponseEntity.ok(profesorService.findByEspecialidad(especialidad, page, size, sort, direction));
    }

    @PostMapping
    public ResponseEntity<ProfesorEntity> createProfesor(@RequestBody ProfesorEntity profesor) {
        return ResponseEntity.status(HttpStatus.CREATED).body(profesorService.saveProfesor(profesor));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProfesorEntity> updateProfesor(@PathVariable Long id,
                                                         @RequestBody ProfesorEntity profesor,
                                                         @RequestParam(defaultValue = "false") boolean syncUsuario) {
        try{
            ProfesorEntity profesorActualizado= profesorService.updateProfesor(id, profesor, syncUsuario);
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
    public ResponseEntity<?> crearwithUser(@RequestBody Map<String, Object> request) {
        try {
            // Extraer los datos del profesor
            ProfesorEntity profesor = new ProfesorEntity();
            profesor.setNombre((String) request.get("nombre"));
            profesor.setApellido((String) request.get("apellido"));
            profesor.setTelefono((String) request.get("telefono"));
            profesor.setEmail((String) request.get("email"));
            profesor.setEspecialidad((String) request.get("especialidad"));

            if (request.get("anhosExperiencia") != null) {
                profesor.setAnhosExperiencia(Integer.valueOf(request.get("anhosExperiencia").toString()));
            }

            // Extraer los datos del usuario
            UsuarioEntity usuario = new UsuarioEntity();
            Map<String, Object> usuarioData = (Map<String, Object>) request.get("usuario");
            usuario.setUsername((String) usuarioData.get("username"));
            usuario.setPassword((String) usuarioData.get("password"));

            // Los nombres se copiarán del profesor automáticamente si no se proporcionan
            if (usuarioData.get("nombre") != null) {
                usuario.setNombre((String) usuarioData.get("nombre"));
            }
            if (usuarioData.get("apellido") != null) {
                usuario.setApellido((String) usuarioData.get("apellido"));
            }

            // Crear el profesor con el usuario asociado
            ProfesorEntity createdProfesor = profesorService.createProfesorWithUser(profesor, usuario);

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