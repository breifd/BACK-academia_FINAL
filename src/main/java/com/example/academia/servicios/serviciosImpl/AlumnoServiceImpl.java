package com.example.academia.servicios.serviciosImpl;

import com.example.academia.DTOs.Created.AlumnoCreateDTO;
import com.example.academia.DTOs.Created.UsuarioCreateDTO;
import com.example.academia.DTOs.Response.AlumnoResponseDTO;
import com.example.academia.DTOs.SimpleDTO.CursoSimpleDTO;
import com.example.academia.Exceptions.ValidationException;
import com.example.academia.entidades.AlumnoEntity;
import com.example.academia.entidades.UsuarioEntity;
import com.example.academia.mappers.AlumnoMapper;
import com.example.academia.mappers.CursoMapper;
import com.example.academia.repositorios.AlumnoRepository;
import com.example.academia.repositorios.CursoRepository;
import com.example.academia.repositorios.UsuarioRepository;
import com.example.academia.servicios.AlumnoService;
import com.example.academia.validators.UsuarioValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AlumnoServiceImpl implements AlumnoService {
    private final AlumnoRepository aRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioValidator usuarioValidator;
    private final CursoRepository cursoRepository;
    private final AlumnoMapper alumnoMapper;
    private final CursoMapper cursoMapper;
    private final PasswordEncoder passwordEncoder;

    private Pageable crearPageable(int page, int size, String sort, String direction) {
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        if (sort == null || sort.isEmpty()) {
            sort = "id";
        }
        return PageRequest.of(page, size, sortDirection, sort);
    }

    @Override
    public Page<AlumnoResponseDTO> findAll(int page, int size, String sort, String direction) {
        Pageable pageable = crearPageable(page, size, sort, direction);
        return aRepository.findAll(pageable).map(alumnoMapper::toAlumnoResponseDTO);
    }

    @Override
    public Optional<AlumnoResponseDTO> findById(Long id) {
        return aRepository.findById(id).map(alumnoMapper::toAlumnoResponseDTO);
    }

    @Override
    public Page<AlumnoResponseDTO> findByNombreOrApellido(String nombre, String apellido, int page, int size, String sort, String direction) {
        Pageable pageable = crearPageable(page, size, sort, direction);
        boolean filtraNombre = nombre != null && !nombre.trim().isEmpty();
        boolean filtraApellido = apellido != null && !apellido.trim().isEmpty();

        Page<AlumnoEntity> alumnosPage;
        if (filtraNombre && filtraApellido) {
            alumnosPage = aRepository.findByNombreContainingIgnoreCaseAndApellidoContainingIgnoreCase(nombre, apellido, pageable);
        } else if (filtraNombre) {
            alumnosPage = aRepository.findByNombreContainingIgnoreCase(nombre, pageable);
        } else if (filtraApellido) {
            alumnosPage = aRepository.findByApellidoContainingIgnoreCase(apellido, pageable);
        } else {
            alumnosPage = aRepository.findAll(pageable);
        }
        return alumnosPage.map(alumnoMapper::toAlumnoResponseDTO);
    }

    @Override
    @Transactional
    public AlumnoResponseDTO saveAlumno(AlumnoCreateDTO alumnoDTO) {
        // CAMBIO: Ahora siempre requiere datos de usuario
        if (alumnoDTO.getUsuario() == null) {
            throw new ValidationException("Los datos de usuario son obligatorios para crear un alumno");
        }

        return createAlumnoWithUser(alumnoDTO);
    }

    @Override
    @Transactional
    public AlumnoResponseDTO updateAlumno(Long id, AlumnoCreateDTO alumno, boolean syncUsuario) {
        Optional<AlumnoEntity> alumnoEncontrado = aRepository.findById(id);
        if (alumnoEncontrado.isEmpty()) {
            throw new ValidationException("No existe ningún alumno con id " + id);
        }

        AlumnoEntity alumnoEntity = alumnoMapper.toAlumnoEntity(alumno);
        alumnoEntity.setId(id);
        AlumnoEntity alumnoActual = aRepository.save(alumnoEntity);

        if (syncUsuario) {
            Optional<UsuarioEntity> usuarioEncontrado = usuarioRepository.findByAlumnoId(id);
            if (usuarioEncontrado.isPresent()) {
                UsuarioEntity usuarioActual = usuarioEncontrado.get();
                usuarioActual.setNombre(alumno.getNombre());
                usuarioActual.setApellido(alumno.getApellido());
                usuarioRepository.save(usuarioActual);
            }
        }
        return alumnoMapper.toAlumnoResponseDTO(alumnoActual);
    }

    @Override
    @Transactional
    public AlumnoResponseDTO createAlumnoWithUser(AlumnoCreateDTO alumnoDTO) {
        try {
            // PASO 1: Crear y guardar el alumno (sin relaciones)
            AlumnoEntity alumnoE = alumnoMapper.toAlumnoEntity(alumnoDTO);
            AlumnoEntity alumnoGuardado = aRepository.save(alumnoE);

            // PASO 2: Crear usuario asociado si se proporciona
            if (alumnoDTO.getUsuario() != null) {
                UsuarioCreateDTO usuarioDTO = alumnoDTO.getUsuario();

                // Validar datos de usuario
                if (usuarioDTO.getUsername() == null || usuarioDTO.getUsername().trim().isEmpty()) {
                    throw new ValidationException("El nombre de usuario es obligatorio");
                }
                if (usuarioDTO.getPassword() == null || usuarioDTO.getPassword().trim().isEmpty()) {
                    throw new ValidationException("La contraseña es obligatoria");
                }

                // Verificar que el username no esté en uso
                if (usuarioRepository.existsByUsername(usuarioDTO.getUsername())) {
                    throw new ValidationException("El nombre de usuario '" + usuarioDTO.getUsername() + "' ya está en uso");
                }

                // Crear usuario manualmente (no usar mapper para evitar problemas de relaciones)
                UsuarioEntity usuario = new UsuarioEntity();
                usuario.setUsername(usuarioDTO.getUsername());
                usuario.setPassword(passwordEncoder.encode(usuarioDTO.getPassword())); // Encriptar contraseña
                usuario.setRol(UsuarioEntity.Rol.Alumno);

                // Configurar nombre y apellido
                usuario.setNombre(usuarioDTO.getNombre() != null && !usuarioDTO.getNombre().trim().isEmpty()
                        ? usuarioDTO.getNombre() : alumnoDTO.getNombre());
                usuario.setApellido(usuarioDTO.getApellido() != null && !usuarioDTO.getApellido().trim().isEmpty()
                        ? usuarioDTO.getApellido() : alumnoDTO.getApellido());

                // IMPORTANTE: Establecer la relación con la entidad MANAGED
                usuario.setAlumno(alumnoGuardado);

                // Validar relaciones antes de guardar
                usuarioValidator.validateRolRelations(usuario);

                // Guardar usuario
                UsuarioEntity usuarioGuardado = usuarioRepository.save(usuario);

                System.out.println("✅ Usuario creado exitosamente: " + usuarioGuardado.getUsername());
            }

            return alumnoMapper.toAlumnoResponseDTO(alumnoGuardado);

        } catch (ValidationException e) {
            System.err.println("❌ Error de validación: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("❌ Error inesperado al crear alumno con usuario: " + e.getMessage());
            e.printStackTrace();
            throw new ValidationException("Error al crear alumno: " + e.getMessage());
        }
    }
    @Override
    public Page<CursoSimpleDTO> getCursosByAlumno(Long alumnoId, int page, int size, String sort, String direction) {
        Pageable pageable = crearPageable(page, size, sort, direction);
        AlumnoEntity alumno = aRepository.findById(alumnoId)
                .orElseThrow(() -> new ValidationException("No existe ningún alumno con id " + alumnoId));
        return cursoRepository.findByAlumnosId(alumnoId, pageable).map(cursoMapper::toCursoSimpleDTO);
    }

    @Override
    @Transactional
    public void deleteAlumno(Long id) {
        // Verificar que el alumno existe
        if (!aRepository.existsById(id)) {
            throw new ValidationException("No existe ningún alumno con id " + id);
        }

        // Eliminar usuario asociado si existe
        Optional<UsuarioEntity> usuario = usuarioRepository.findByAlumnoId(id);
        if (usuario.isPresent()) {
            usuarioRepository.delete(usuario.get());
        }

        // Eliminar alumno
        aRepository.deleteById(id);
    }
}