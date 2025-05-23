package com.example.academia.servicios.serviciosImpl;

import com.example.academia.DTOs.LoginResponse;
import com.example.academia.DTOs.Response.UsuarioResponseDTO;
import com.example.academia.DTOs.UsuarioDTO;
import com.example.academia.DTOs.Created.UsuarioCreateDTO;
import com.example.academia.Exceptions.ValidationException;
import com.example.academia.entidades.AlumnoEntity;
import com.example.academia.entidades.ProfesorEntity;
import com.example.academia.entidades.UsuarioEntity;
import com.example.academia.mappers.UsuarioMapper;
import com.example.academia.repositorios.AlumnoRepository;
import com.example.academia.repositorios.ProfesorRepository;
import com.example.academia.repositorios.UsuarioRepository;
import com.example.academia.servicios.UsuarioService;
import com.example.academia.validators.UsuarioValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final ProfesorRepository profesorRepository;
    private final UsuarioRepository usuarioRepository;
    private final AlumnoRepository alumnoRepository;
    private final UsuarioValidator usuarioValidator;
    private final UsuarioMapper usuarioMapper;

    @Override
    public Optional<UsuarioResponseDTO> findByUsername(String username) {
        return usuarioRepository.findByUsername(username)
                .map(usuarioMapper::toUsuarioResponseDTO);
    }

    @Override
    public List<UsuarioResponseDTO> findAll() {
        return usuarioRepository.findAll().stream()
                .map(usuarioMapper::toUsuarioResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public LoginResponse login(String username, String password) {
        try {
            // Validamos campos
            usuarioValidator.validateUsername(username);
            usuarioValidator.validatePassword(password);

            // Buscamos el usuario por username
            Optional<UsuarioEntity> usuario = usuarioRepository.findByUsername(username);
            if (usuario.isEmpty()) {
                return LoginResponse.error("Usuario o contraseña incorrectos", "INVALID_CREDENTIALS");
            }

            // Una vez encontrado el usuario vamos a comprobar las contraseñas
            UsuarioEntity usuarioEntity = usuario.get();

            if (!usuarioEntity.getPassword().equals(password)) {
                return LoginResponse.error("Usuario o contraseña incorrectos", "INVALID_CREDENTIALS");
            }

            // Obtenemos los ids relacionados para saber que tipo de usuario es
            Long profesorId = usuarioEntity.getProfesor() != null ? usuarioEntity.getProfesor().getId() : null;
            Long alumnoId = usuarioEntity.getAlumno() != null ? usuarioEntity.getAlumno().getId() : null;

            // Login exitoso con información extendida
            return LoginResponse.success(
                    usuarioEntity.getUsername(),
                    usuarioEntity.getNombre(),
                    usuarioEntity.getApellido(),
                    usuarioEntity.getRol(),
                    profesorId,
                    alumnoId
            );

        } catch (ValidationException e) {
            return LoginResponse.error(e.getMessage(), "VALIDATION_ERROR");
        } catch (Exception e) {
            return LoginResponse.error("Error durante el proceso de login", "INTERNAL_ERROR");
        }
    }

    @Override
    public UsuarioResponseDTO saveUsuario(UsuarioDTO usuario) {
        UsuarioEntity usuarioEntity = usuarioMapper.toUsuarioEntity(usuario);
        usuarioValidator.validateRolRelations(usuarioEntity);
        UsuarioEntity savedUsuario = usuarioRepository.save(usuarioEntity);
        return usuarioMapper.toUsuarioResponseDTO(savedUsuario);
    }

    @Override
    @Transactional
    public UsuarioResponseDTO createUsuario(UsuarioCreateDTO usuarioDTO) {
        // Comprobamos si existe el usuario en la base de datos
        if (usuarioRepository.existsByUsername(usuarioDTO.getUsername())) {
            throw new ValidationException("El nombre de usuario ya está en uso");
        }

        // Crear usuario a partir del DTO
        UsuarioEntity usuarioEntity = usuarioMapper.createUsuarioFromDTO(usuarioDTO);

        // Establecer el rol y relaciones según corresponda
        if (usuarioDTO.getRol() == UsuarioEntity.Rol.Profesor && usuarioDTO.getProfesorId() != null) {
            ProfesorEntity profesor = profesorRepository.findById(usuarioDTO.getProfesorId())
                    .orElseThrow(() -> new ValidationException("El profesor con ID " + usuarioDTO.getProfesorId() + " no existe"));

            if (usuarioRepository.findByProfesorId(usuarioDTO.getProfesorId()).isPresent()) {
                throw new ValidationException("El profesor con ID " + usuarioDTO.getProfesorId() + " ya está asociado a un usuario");
            }

            usuarioEntity.setProfesor(profesor);
        }

        if (usuarioDTO.getRol() == UsuarioEntity.Rol.Alumno && usuarioDTO.getAlumnoId() != null) {
            AlumnoEntity alumno = alumnoRepository.findById(usuarioDTO.getAlumnoId())
                    .orElseThrow(() -> new ValidationException("El alumno con ID " + usuarioDTO.getAlumnoId() + " no existe"));

            if (usuarioRepository.findByAlumnoId(usuarioDTO.getAlumnoId()).isPresent()) {
                throw new ValidationException("El alumno con ID " + usuarioDTO.getAlumnoId() + " ya está asociado a un usuario");
            }

            usuarioEntity.setAlumno(alumno);
        }

        usuarioValidator.validateRolRelations(usuarioEntity);
        UsuarioEntity savedUsuario = usuarioRepository.save(usuarioEntity);
        return usuarioMapper.toUsuarioResponseDTO(savedUsuario);
    }

    @Override
    @Transactional
    public UsuarioResponseDTO updateUsuario(Long id, UsuarioDTO usuarioDTO) {
        UsuarioEntity usuarioEntity = usuarioRepository.findById(id)
                .orElseThrow(() -> new ValidationException("El usuario con ID " + id + " no existe"));

        // No se permite cambiar el username si ya existe
        if (!usuarioEntity.getUsername().equals(usuarioDTO.getUsername()) &&
                usuarioRepository.existsByUsername(usuarioDTO.getUsername())) {
            throw new ValidationException("El nombre de usuario " + usuarioDTO.getUsername() + " ya está en uso");
        }

        // Actualizar usuario con los nuevos datos
        usuarioMapper.updateUsuarioFromDTO(usuarioDTO, usuarioEntity);

        // Manejar cambio de rol y relaciones
        if (usuarioEntity.getRol() != usuarioDTO.getRol()) {
            // Si cambia de rol, eliminar relaciones anteriores
            if (usuarioEntity.getRol() == UsuarioEntity.Rol.Profesor) {
                usuarioEntity.setProfesor(null);
            } else if (usuarioEntity.getRol() == UsuarioEntity.Rol.Alumno) {
                usuarioEntity.setAlumno(null);
            }

            usuarioEntity.setRol(usuarioDTO.getRol());
        }

        // Actualizar relación con Profesor si corresponde
        if (usuarioDTO.getRol() == UsuarioEntity.Rol.Profesor && usuarioDTO.getProfesorId() != null) {
            ProfesorEntity profesor = profesorRepository.findById(usuarioDTO.getProfesorId())
                    .orElseThrow(() -> new ValidationException("El profesor con ID " + usuarioDTO.getProfesorId() + " no existe"));

            // Verificar que el profesor no esté ya asociado a otro usuario
            Optional<UsuarioEntity> usuarioExistente = usuarioRepository.findByProfesorId(usuarioDTO.getProfesorId());
            if (usuarioExistente.isPresent() && !usuarioExistente.get().getId().equals(id)) {
                throw new ValidationException("El profesor con ID " + usuarioDTO.getProfesorId() +
                        " ya está asociado al usuario " + usuarioExistente.get().getUsername());
            }

            usuarioEntity.setProfesor(profesor);
        } else if (usuarioDTO.getRol() == UsuarioEntity.Rol.Profesor) {
            // Si tiene rol profesor pero no se especifica ID, mantener la relación anterior
        } else {
            usuarioEntity.setProfesor(null);
        }

        // Actualizar relación con Alumno si corresponde
        if (usuarioDTO.getRol() == UsuarioEntity.Rol.Alumno && usuarioDTO.getAlumnoId() != null) {
            AlumnoEntity alumno = alumnoRepository.findById(usuarioDTO.getAlumnoId())
                    .orElseThrow(() -> new ValidationException("El alumno con ID " + usuarioDTO.getAlumnoId() + " no existe"));

            // Verificar que el alumno no esté ya asociado a otro usuario
            Optional<UsuarioEntity> usuarioExistente = usuarioRepository.findByAlumnoId(usuarioDTO.getAlumnoId());
            if (usuarioExistente.isPresent() && !usuarioExistente.get().getId().equals(id)) {
                throw new ValidationException("El alumno con ID " + usuarioDTO.getAlumnoId() +
                        " ya está asociado al usuario " + usuarioExistente.get().getUsername());
            }

            usuarioEntity.setAlumno(alumno);
        } else if (usuarioDTO.getRol() == UsuarioEntity.Rol.Alumno) {
            // Si tiene rol alumno pero no se especifica ID, mantener la relación anterior
        } else {
            usuarioEntity.setAlumno(null);
        }

        // Validar relaciones según el rol
        usuarioValidator.validateRolRelations(usuarioEntity);

        UsuarioEntity savedUsuario = usuarioRepository.save(usuarioEntity);
        return usuarioMapper.toUsuarioResponseDTO(savedUsuario);
    }

    @Override
    @Transactional
    public void deleteUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }

    @Override
    public List<UsuarioResponseDTO> findByRol(String rol) {
        try {
            UsuarioEntity.Rol rolEnum = UsuarioEntity.Rol.valueOf(rol);
            return usuarioRepository.findByRol(rolEnum).stream()
                    .map(usuarioMapper::toUsuarioResponseDTO)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Rol no válido: " + rol);
        }
    }

    @Override
    public Optional<UsuarioResponseDTO> findByProfesorId(Long profesorId) {
        return usuarioRepository.findByProfesorId(profesorId)
                .map(usuarioMapper::toUsuarioResponseDTO);
    }

    @Override
    public Optional<UsuarioResponseDTO> findByAlumnoId(Long alumnoId) {
        return usuarioRepository.findByAlumnoId(alumnoId)
                .map(usuarioMapper::toUsuarioResponseDTO);
    }

    @Override
    @Transactional
    public UsuarioResponseDTO syncNameWithRelatedEntity(Long usuarioId) {
        UsuarioEntity usuarioEntity = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ValidationException("El usuario con ID " + usuarioId + " no existe"));

        if (usuarioEntity.getRol() == UsuarioEntity.Rol.Profesor && usuarioEntity.getProfesor() != null) {
            ProfesorEntity profesor = usuarioEntity.getProfesor();
            usuarioEntity.setNombre(profesor.getNombre());
            usuarioEntity.setApellido(profesor.getApellido());
        } else if (usuarioEntity.getRol() == UsuarioEntity.Rol.Alumno && usuarioEntity.getAlumno() != null) {
            AlumnoEntity alumno = usuarioEntity.getAlumno();
            usuarioEntity.setNombre(alumno.getNombre());
            usuarioEntity.setApellido(alumno.getApellido());
        }

        UsuarioEntity savedUsuario = usuarioRepository.save(usuarioEntity);
        return usuarioMapper.toUsuarioResponseDTO(savedUsuario);
    }
}