package com.example.academia.servicios.serviciosImpl;

import com.example.academia.DTOs.Created.ProfesorCreateDTO;
import com.example.academia.DTOs.Created.UsuarioCreateDTO;
import com.example.academia.DTOs.Response.ProfesorResponseDTO;
import com.example.academia.DTOs.SimpleDTO.CursoSimpleDTO;
import com.example.academia.Exceptions.ValidationException;
import com.example.academia.entidades.CursoEntity;
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
import org.springframework.stereotype.Service;

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
    public ProfesorResponseDTO createProfesorWithUser(ProfesorCreateDTO profesorDTO) {
        // Guardamos el profesor
        ProfesorEntity profesorE = profesorMapper.toProfesorEntity(profesorDTO);
        ProfesorEntity profesorActual = profesorRepository.save(profesorE);

        // Solo creamos usuario si se incluye en el DTO
        if (profesorDTO.getUsuario() != null) {
            UsuarioCreateDTO usuarioDTO = profesorDTO.getUsuario();

            UsuarioEntity usuario = new UsuarioEntity();
            usuario.setUsername(usuarioDTO.getUsername());
            usuario.setPassword(usuarioDTO.getPassword());
            usuario.setRol(UsuarioEntity.Rol.Profesor);
            usuario.setProfesor(profesorActual);

            // Copiar nombre/apellido del profesor si no están definidos en el usuario
            usuario.setNombre(usuarioDTO.getNombre() != null ? usuarioDTO.getNombre() : profesorDTO.getNombre());
            usuario.setApellido(usuarioDTO.getApellido() != null ? usuarioDTO.getApellido() : profesorDTO.getApellido());

            usuarioValidator.validateRolRelations(usuario);
            usuarioRepository.save(usuario);
        }

        return profesorMapper.toProfesorResponseDTO(profesorActual);
    }

    @Override
    public ProfesorResponseDTO saveProfesor(ProfesorCreateDTO profesor) {
        ProfesorEntity profesorEntity = profesorMapper.toProfesorEntity(profesor);
        ProfesorEntity savedProfesor = profesorRepository.save(profesorEntity);
        return profesorMapper.toProfesorResponseDTO(savedProfesor);
    }

    @Override
    public void deleteProfesor(Long id) {
        // Primero miramos si está relacionado con el usuario y eliminamos la relación
        Optional<UsuarioEntity> usuarioOpt = usuarioRepository.findByProfesorId(id);
        if (usuarioOpt.isPresent()) {
            UsuarioEntity usuario = usuarioOpt.get();
            usuario.setProfesor(null);
            usuarioRepository.save(usuario);
        }

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