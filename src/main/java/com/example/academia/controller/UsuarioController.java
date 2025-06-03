package com.example.academia.controller;

import com.example.academia.DTOs.Created.UsuarioCreateDTO;
import com.example.academia.DTOs.LoginResponse;
import com.example.academia.DTOs.Response.UsuarioResponseDTO;
import com.example.academia.DTOs.UsuarioDTO;
import com.example.academia.Exceptions.ValidationException;
import com.example.academia.entidades.UsuarioEntity;
import com.example.academia.repositorios.UsuarioRepository;
import com.example.academia.security.CustomUserDetails;
import com.example.academia.security.JwtUtil;
import com.example.academia.servicios.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/bcrypt-info")
    public String getAdminHash () {
            return "Hash para admin123: " + passwordEncoder.encode("admin123");
    }
    @GetMapping("/test-auth")
    public String testAuth() {
        try {
            // Usuario para probar
            String username = "admin";
            String rawPassword = "admin123";

            // Buscar el usuario
            Optional<UsuarioEntity> usuario = usuarioRepository.findByUsername(username);
            if (usuario.isEmpty()) {
                return "Usuario no encontrado";
            }

            // Verificar la contraseña
            boolean matches = passwordEncoder.matches(rawPassword, usuario.get().getPassword());

            return "Usuario encontrado. La contraseña " +
                    (matches ? "SÍ" : "NO") +
                    " coincide con el hash. Hash en BD: " +
                    usuario.get().getPassword();
        } catch (Exception e) {
            return "Error durante la prueba: " + e.getMessage();
        }
    }
    @PostMapping("/debug-login")
    public ResponseEntity<?> debugLogin(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        Map<String, Object> debug = new HashMap<>();
        debug.put("username_recibido", username);
        debug.put("password_recibido", password);

        // Buscar usuario
        Optional<UsuarioEntity> usuarioOpt = usuarioRepository.findByUsername(username);
        if (usuarioOpt.isEmpty()) {
            debug.put("usuario_encontrado", false);
            debug.put("error", "Usuario no existe");
            return ResponseEntity.ok(debug);
        }

        UsuarioEntity user = usuarioOpt.get();
        debug.put("usuario_encontrado", true);
        debug.put("usuario_id", user.getId());
        debug.put("usuario_rol", user.getRol());
        debug.put("hash_en_bd", user.getPassword());

        // Verificar contraseña
        boolean passwordMatches = passwordEncoder.matches(password, user.getPassword());
        debug.put("password_coincide", passwordMatches);

        if (passwordMatches) {
            debug.put("resultado", "✅ LOGIN CORRECTO");
        } else {
            debug.put("resultado", "❌ CONTRASEÑA INCORRECTA");
        }

        return ResponseEntity.ok(debug);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        try {
            // Autenticar con Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            // Obtener detalles del usuario autenticado
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            UsuarioEntity usuario = userDetails.getUsuario();

            // Generar token JWT
            String token = jwtUtil.generateToken(
                    usuario.getUsername(),
                    usuario.getRol().name(),
                    userDetails.getProfesorId(),
                    userDetails.getAlumnoId()
            );

            // Crear respuesta exitosa con token
            LoginResponse response = LoginResponse.success(
                    usuario.getUsername(),
                    usuario.getNombre(),
                    usuario.getApellido(),
                    usuario.getRol(),
                    userDetails.getProfesorId(),
                    userDetails.getAlumnoId(),
                    token
            );
            System.out.println(response);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace(); // Para ver en consola el error real
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(LoginResponse.error("Error durante el proceso de login: " + e.getMessage(), "INTERNAL_ERROR"));
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

    // Endpoint para obtener información del usuario actual desde el JWT
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Usuario no autenticado"));
            }

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            UsuarioEntity usuario = userDetails.getUsuario();

            // Crear respuesta con datos del usuario actual
            LoginResponse response = LoginResponse.success(
                    usuario.getUsername(),
                    usuario.getNombre(),
                    usuario.getApellido(),
                    usuario.getRol(),
                    userDetails.getProfesorId(),
                    userDetails.getAlumnoId()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener información del usuario"));
        }
    }

    // Endpoint para refrescar token
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Usuario no autenticado"));
            }

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            UsuarioEntity usuario = userDetails.getUsuario();

            // Generar nuevo token
            String newToken = jwtUtil.generateToken(
                    usuario.getUsername(),
                    usuario.getRol().name(),
                    userDetails.getProfesorId(),
                    userDetails.getAlumnoId()
            );

            return ResponseEntity.ok(Map.of(
                    "token", newToken,
                    "tokenType", "Bearer"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al refrescar el token"));
        }
    }
    @PutMapping("/cambiar-password-simple")
    public ResponseEntity<?> cambiarPasswordSimple(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String passwordActual = request.get("passwordActual");
            String passwordNueva = request.get("passwordNueva");

            // Buscar usuario
            Optional<UsuarioEntity> usuarioOpt = usuarioRepository.findByUsername(username);
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Usuario no encontrado"));
            }

            UsuarioEntity usuario = usuarioOpt.get();

            // Verificar contraseña actual
            if (!passwordEncoder.matches(passwordActual, usuario.getPassword())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "La contraseña actual no es correcta"));
            }

            // Actualizar contraseña
            usuario.setPassword(passwordEncoder.encode(passwordNueva));
            usuarioRepository.save(usuario);

            return ResponseEntity.ok(Map.of("success", true, "message", "Contraseña actualizada correctamente"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al cambiar contraseña: " + e.getMessage()));
        }
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