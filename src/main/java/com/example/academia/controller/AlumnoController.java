package com.example.academia.controller;

import com.example.academia.Exceptions.ValidationException;
import com.example.academia.entidades.AlumnoEntity;
import com.example.academia.entidades.UsuarioEntity;
import com.example.academia.servicios.AlumnoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("api/alumnos")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class AlumnoController {

    private final AlumnoService alumnoService;

    @PostMapping
    public ResponseEntity<AlumnoEntity> createAlumno(@RequestBody AlumnoEntity alumno) {
        return ResponseEntity.status(HttpStatus.CREATED).body(alumnoService.saveAlumno(alumno)); // Te devuelve el codigo 201 creado con la entidad enviada en el cuerpo
    }
    @PostMapping("/con-usuario")
    public ResponseEntity<?> createAlumnoWithUser(
            @RequestBody Map<String, Object> request) {
        try {
            // Extraer los datos del alumno
            AlumnoEntity alumno = new AlumnoEntity();
            alumno.setNombre((String) request.get("nombre"));
            alumno.setApellido((String) request.get("apellido"));
            alumno.setTelefono((String) request.get("telefono"));
            alumno.setEmail((String) request.get("email"));
            alumno.setDireccion((String) request.get("direccion"));

            // Convertir fecha de nacimiento si existe
            if (request.get("fechaNacimiento") != null) {
                alumno.setFechaNacimiento(LocalDate.parse((String) request.get("fechaNacimiento")));
            }

            // Extraer los datos del usuario
            UsuarioEntity usuario = new UsuarioEntity();
            Map<String, Object> usuarioData = (Map<String, Object>) request.get("usuario");
            usuario.setUsername((String) usuarioData.get("username"));
            usuario.setPassword((String) usuarioData.get("password"));

            // Los nombres se copiarán del alumno automáticamente si no se proporcionan
            if (usuarioData.get("nombre") != null) {
                usuario.setNombre((String) usuarioData.get("nombre"));
            }
            if (usuarioData.get("apellido") != null) {
                usuario.setApellido((String) usuarioData.get("apellido"));
            }

            // Crear el alumno con el usuario asociado
            AlumnoEntity createdAlumno = alumnoService.createAlumnoWithUser(alumno, usuario);

            return ResponseEntity.status(HttpStatus.CREATED).body(createdAlumno);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    @GetMapping
    public ResponseEntity<Page<AlumnoEntity>> getAllAlumnos(@RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "10") int size,
                                                            @RequestParam(defaultValue = "id") String sort,
                                                            @RequestParam(defaultValue = "asc" ) String direction) {
        return ResponseEntity.ok(alumnoService.findAll(page, size, sort, direction));
    }
    @GetMapping("/{id}")
    public ResponseEntity<AlumnoEntity> getAlumnoById(@PathVariable long id) {
        return alumnoService.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/buscar")
    public ResponseEntity<Page<AlumnoEntity>> buscarAlumnoPorNombreOApellido(@RequestParam String nombre,
                                                                       @RequestParam String apellido,
                                                                       @RequestParam(defaultValue = "0") int page,
                                                                       @RequestParam(defaultValue = "10") int size,
                                                                       @RequestParam(defaultValue = "id") String sort,
                                                                       @RequestParam(defaultValue = "asc" ) String direction) {
        return ResponseEntity.ok(alumnoService.findByNombreOrApellido(nombre,apellido,page,size,sort,direction));
    }
    @PutMapping("/{id}")
    public ResponseEntity<AlumnoEntity> updateAlumno(@PathVariable long id,
                                                     @RequestBody AlumnoEntity alumno,
                                                     @RequestParam(defaultValue = "false") boolean syncUsuario) {
        //404 si no existe, comprobamos si existe un alumno con ese id, si es asi marcamos la entidad enviada con el id del encontrada y guardamos
        try {
            AlumnoEntity updatedAlumno = alumnoService.updateAlumno(id, alumno, syncUsuario);
            return ResponseEntity.ok(updatedAlumno);
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlumno(@PathVariable long id) {
        return alumnoService.findById(id).map(
                alumno->{
                    alumnoService.deleteAlumno(id);
                    return ResponseEntity.noContent().<Void>build();
                }).orElse(ResponseEntity.notFound().build());
    }
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(ValidationException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }


}
