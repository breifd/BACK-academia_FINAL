package com.example.academia.controller;

import com.example.academia.DTOs.Created.UsuarioCreateDTO;
import com.example.academia.DTOs.LoginResponse;
import com.example.academia.DTOs.Response.UsuarioResponseDTO;
import com.example.academia.DTOs.UsuarioDTO;
import com.example.academia.Exceptions.ValidationException;
import com.example.academia.entidades.UsuarioEntity;
import com.example.academia.repositorios.UsuarioRepository;
import com.example.academia.servicios.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador que maneja las peticiones relacionadas con los usuarios
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        // Utilizamos el método de login mejorado del servicio
        LoginResponse loginResponse = usuarioService.login(username, password);

        if (loginResponse.isSuccess()) {
            return ResponseEntity.ok(loginResponse);
        } else {
            // Devolvemos un código 400 (Bad Request) para credenciales inválidas
            return ResponseEntity.badRequest().body(loginResponse);
        }
    }

    @GetMapping("/usuario/{username}")
    public ResponseEntity<?> getUsuario(@PathVariable String username) {
        return usuarioService.findByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/usuarios")
    public ResponseEntity<List<UsuarioResponseDTO>> getAllUsuarios() {
        return ResponseEntity.ok(usuarioService.findAll());
    }

    @GetMapping("/usuarios/rol/{rol}")
    public ResponseEntity<List<UsuarioResponseDTO>> getUsuariosByRol(@PathVariable String rol) {
        return ResponseEntity.ok(usuarioService.findByRol(rol));
    }

    @PostMapping("/usuarios")
    public ResponseEntity<?> createUsuario(@RequestBody UsuarioCreateDTO usuarioDTO) {
        try {
            UsuarioResponseDTO newUsuario = usuarioService.createUsuario(usuarioDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(newUsuario);
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/usuarios/{id}")
    public ResponseEntity<?> updateUsuario(@PathVariable Long id, @RequestBody UsuarioDTO usuarioDTO) {
        try {
            UsuarioResponseDTO updatedUsuario = usuarioService.updateUsuario(id, usuarioDTO);
            return ResponseEntity.ok(updatedUsuario);
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/usuarios/{id}/sync-name")
    public ResponseEntity<?> syncUsuarioName(@PathVariable Long id) {
        try {
            UsuarioResponseDTO syncedUsuario = usuarioService.syncNameWithRelatedEntity(id);
            return ResponseEntity.ok(syncedUsuario);
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<Void> deleteUsuario(@PathVariable Long id) {
        usuarioService.deleteUsuario(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/usuarios/profesor/{profesorId}")
    public ResponseEntity<?> getUsuarioByProfesorId(@PathVariable Long profesorId) {
        return usuarioService.findByProfesorId(profesorId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/usuarios/alumno/{alumnoId}")
    public ResponseEntity<?> getUsuarioByAlumnoId(@PathVariable Long alumnoId) {
        return usuarioService.findByAlumnoId(alumnoId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/check-username/{username}")
    public ResponseEntity<Boolean> checkUsernameExists(@PathVariable String username) {
        boolean exists = usuarioRepository.existsByUsername(username);
        return ResponseEntity.ok(exists);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(ValidationException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
    }
}