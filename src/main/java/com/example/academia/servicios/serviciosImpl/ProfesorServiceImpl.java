package com.example.academia.servicios.serviciosImpl;

import com.example.academia.DTOs.Created.ProfesorCreateDTO;
import com.example.academia.DTOs.Created.UsuarioCreateDTO;
import com.example.academia.DTOs.Response.ProfesorResponseDTO;
import com.example.academia.DTOs.SimpleDTO.CursoSimpleDTO;
import com.example.academia.Exceptions.ValidationException;
import com.example.academia.entidades.ProfesorEntity;
import com.example.academia.entidades.UsuarioEntity;
import com.example.academia.mappers.CursoMapper;
import com.example.academia.mappers.ProfesorMapper;
import com.example.academia.repositorios.CursoRepository;
import com.example.academia.repositorios.ProfesorRepository;
import com.example.academia.repositorios.UsuarioRepository;
import com.example.academia.servicios.ProfesorService;
import com.example.academia.validators.UsuarioValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfesorServiceImpl implements ProfesorService {

    private final ProfesorRepository profesorRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioValidator usuarioValidator;
    private final CursoRepository cursoRepository;
    private final ProfesorMapper profesorMapper;
    private final CursoMapper cursoMapper;
    private final PasswordEncoder passwordEncoder;

    private Pageable createPageable(int page, int size, String sort, String direction) {
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        if (sort == null || sort.isEmpty()) {
            sort = "id";
        }
        return PageRequest.of(page, size, sortDirection, sort);
    }

    @Override
    public Page<ProfesorResponseDTO> findAll(int page, int size, String sort, String direction) {
        Pageable pageable = createPageable(page, size, sort, direction);
        return profesorRepository.findAll(pageable).map(profesorMapper::toProfesorResponseDTO);
    }

    @Override
    public Optional<ProfesorResponseDTO> findById(Long id) {
        return profesorRepository.findById(id).map(profesorMapper::toProfesorResponseDTO);
    }

    @Override
    public List<ProfesorResponseDTO> findAllLista() {
        return profesorRepository.findAll().stream()
                .map(profesorMapper::toProfesorResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ProfesorResponseDTO> findByNombreOrApellido(String nombre, String apellido, int page, int size, String sort, String direction) {
        Pageable pageable = createPageable(page, size, sort, direction);
        boolean filtraNombre = nombre != null && !nombre.trim().isEmpty();
        boolean filtraApellido = apellido != null && !apellido.trim().isEmpty();

        Page<ProfesorEntity> profesoresPage;
        if (filtraNombre && filtraApellido) {
            profesoresPage = profesorRepository.findByNombreContainingIgnoreCaseAndApellidoContainingIgnoreCase(nombre, apellido, pageable);
        } else if (filtraNombre) {
            profesoresPage = profesorRepository.findByNombreContainingIgnoreCase(nombre, pageable);
        } else if (filtraApellido) {
            profesoresPage = profesorRepository.findByApellidoContainingIgnoreCase(apellido, pageable);
        } else {
            profesoresPage = profesorRepository.findAll(pageable);
        }

        return profesoresPage.map(profesorMapper::toProfesorResponseDTO);
    }

    @Override
    public Page<ProfesorResponseDTO> findByEspecialidad(String especialidad, int page, int size, String sort, String direction) {
        Pageable pageable = createPageable(page, size, sort, direction);
        return profesorRepository.findByEspecialidadIgnoreCase(especialidad, pageable)
                .map(profesorMapper::toProfesorResponseDTO);
    }

    @Override
    @Transactional
    public ProfesorResponseDTO updateProfesor(Long id, ProfesorCreateDTO profesor, boolean syncUsuario) {
        Optional<ProfesorEntity> profesorEntity = profesorRepository.findById(id);
        if (profesorEntity.isEmpty()) {
            throw new ValidationException("El profesor con ID " + id + " no existe.");
        }

        ProfesorEntity profesorToUpdate = profesorMapper.toProfesorEntity(profesor);
        profesorToUpdate.setId(id);
        ProfesorEntity profesorActualizado = profesorRepository.save(profesorToUpdate);

        // Si se solicita sincronización, actualizar el usuario
        if (syncUsuario) {
            Optional<UsuarioEntity> usuario = usuarioRepository.findByProfesorId(id);
            if (usuario.isPresent()) {
                UsuarioEntity usuarioActualizado = usuario.get();
                usuarioActualizado.setNombre(profesorActualizado.getNombre());
                usuarioActualizado.setApellido(profesorActualizado.getApellido());
                usuarioRepository.save(usuarioActualizado);
            }
        }

        return profesorMapper.toProfesorResponseDTO(profesorActualizado);
    }

    @Override
    @Transactional
    public ProfesorResponseDTO createProfesorWithUser(ProfesorCreateDTO profesorDTO) {
        try {
            // PASO 1: Crear y guardar el profesor (sin relaciones)
            ProfesorEntity profesorE = profesorMapper.toProfesorEntity(profesorDTO);
            ProfesorEntity profesorGuardado = profesorRepository.save(profesorE);

            // PASO 2: Crear usuario asociado si se proporciona
            if (profesorDTO.getUsuario() != null) {
                UsuarioCreateDTO usuarioDTO = profesorDTO.getUsuario();

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
                usuario.setRol(UsuarioEntity.Rol.Profesor);

                // Configurar nombre y apellido
                usuario.setNombre(usuarioDTO.getNombre() != null && !usuarioDTO.getNombre().trim().isEmpty()
                        ? usuarioDTO.getNombre() : profesorDTO.getNombre());
                usuario.setApellido(usuarioDTO.getApellido() != null && !usuarioDTO.getApellido().trim().isEmpty()
                        ? usuarioDTO.getApellido() : profesorDTO.getApellido());

                // IMPORTANTE: Establecer la relación con la entidad MANAGED
                usuario.setProfesor(profesorGuardado);

                // Validar relaciones antes de guardar
                usuarioValidator.validateRolRelations(usuario);

                // Guardar usuario
                UsuarioEntity usuarioGuardado = usuarioRepository.save(usuario);

                System.out.println("✅ Usuario creado exitosamente: " + usuarioGuardado.getUsername());
            }

            return profesorMapper.toProfesorResponseDTO(profesorGuardado);

        } catch (ValidationException e) {
            System.err.println("❌ Error de validación: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("❌ Error inesperado al crear profesor con usuario: " + e.getMessage());
            e.printStackTrace();
            throw new ValidationException("Error al crear profesor: " + e.getMessage());
        }
    }


    @Override
    @Transactional
    public ProfesorResponseDTO saveProfesor(ProfesorCreateDTO profesorDTO) {
        // CAMBIO: Ahora siempre requiere datos de usuario
        if (profesorDTO.getUsuario() == null) {
            throw new ValidationException("Los datos de usuario son obligatorios para crear un profesor");
        }

        return createProfesorWithUser(profesorDTO);
    }

    @Override
    @Transactional
    public void deleteProfesor(Long id) {
        // Verificar que el profesor existe
        if (!profesorRepository.existsById(id)) {
            throw new ValidationException("No existe ningún profesor con id " + id);
        }

        // Eliminar usuario asociado si existe
        Optional<UsuarioEntity> usuarioOpt = usuarioRepository.findByProfesorId(id);
        if (usuarioOpt.isPresent()) {
            usuarioRepository.delete(usuarioOpt.get());
        }

        // Eliminar profesor
        profesorRepository.deleteById(id);
    }

    @Override
    public Page<CursoSimpleDTO> getCursosByProfesor(Long profesorId, int page, int size, String sort, String direction) {
        Pageable pageable = createPageable(page, size, sort, direction);
        ProfesorEntity profesor = profesorRepository.findById(profesorId)
                .orElseThrow(() -> new ValidationException("No existe ningún profesor con id " + profesorId));
        return cursoRepository.findByProfesoresId(profesorId, pageable).map(cursoMapper::toCursoSimpleDTO);
    }
}