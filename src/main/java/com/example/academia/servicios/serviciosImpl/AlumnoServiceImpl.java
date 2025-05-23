package com.example.academia.servicios.serviciosImpl;

import com.example.academia.DTOs.Created.AlumnoCreateDTO;
import com.example.academia.DTOs.Created.UsuarioCreateDTO;
import com.example.academia.DTOs.Response.AlumnoResponseDTO;
import com.example.academia.DTOs.SimpleDTO.CursoSimpleDTO;
import com.example.academia.Exceptions.ValidationException;
import com.example.academia.entidades.AlumnoEntity;
import com.example.academia.entidades.CursoEntity;
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
import org.springframework.stereotype.Service;

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

    private Pageable crearPageable(int page, int size, String sort, String direction) {
        Sort.Direction sortDirection=direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        if (sort == null || sort.isEmpty()) {
            sort = "id";  // O cualquier campo predeterminado de tu entidad
        }
        return PageRequest.of(page, size, sortDirection,sort);
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
            alumnosPage= aRepository.findByNombreContainingIgnoreCaseAndApellidoContainingIgnoreCase(nombre, apellido, pageable);
        } else if (filtraNombre) {
            alumnosPage= aRepository.findByNombreContainingIgnoreCase(nombre, pageable);
        } else if (filtraApellido) {
            alumnosPage= aRepository.findByApellidoContainingIgnoreCase(apellido, pageable);
        } else {
            alumnosPage= aRepository.findAll(pageable);
        }
        return alumnosPage.map(alumnoMapper::toAlumnoResponseDTO);
    }

    @Override
    public AlumnoResponseDTO saveAlumno(AlumnoCreateDTO alumno) {
        AlumnoEntity alumnoEntity= alumnoMapper.toAlumnoEntity(alumno);
        return alumnoMapper.toAlumnoResponseDTO(aRepository.save(alumnoEntity));
    }

    @Override
    public AlumnoResponseDTO updateAlumno(Long id, AlumnoCreateDTO alumno, boolean syncUsuario) {
       Optional<AlumnoEntity> alumnoEncontrado = aRepository.findById(id);
       if (alumnoEncontrado.isEmpty()) {
           throw new ValidationException("No existe ningun alumno con id " + id);
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
    public AlumnoResponseDTO createAlumnoWithUser(AlumnoCreateDTO alumnoDTO) {
        AlumnoEntity alumnoE = alumnoMapper.toAlumnoEntity(alumnoDTO);
        AlumnoEntity alumnoActual = aRepository.save(alumnoE);

        // Solo creamos usuario si se incluye en el DTO
        if (alumnoDTO.getUsuario() != null) {
            UsuarioCreateDTO usuarioDTO = alumnoDTO.getUsuario();

            UsuarioEntity usuario = new UsuarioEntity();
            usuario.setUsername(usuarioDTO.getUsername());
            usuario.setPassword(usuarioDTO.getPassword());
            usuario.setRol(UsuarioEntity.Rol.Alumno);
            usuario.setAlumno(alumnoActual);

            // Copiar nombre/apellido del alumno si no están definidos en el usuario
            usuario.setNombre(usuarioDTO.getNombre() != null ? usuarioDTO.getNombre() : alumnoDTO.getNombre());
            usuario.setApellido(usuarioDTO.getApellido() != null ? usuarioDTO.getApellido() : alumnoDTO.getApellido());

            usuarioValidator.validateRolRelations(usuario);
            usuarioRepository.save(usuario);
        }

        return alumnoMapper.toAlumnoResponseDTO(alumnoActual);
    }

    @Override
    public Page<CursoSimpleDTO> getCursosByAlumno(Long alumnoId, int page, int size, String sort, String direction) {
        Pageable pageable = crearPageable(page, size, sort, direction);
        AlumnoEntity alumno = aRepository.findById(alumnoId).orElseThrow(()-> new ValidationException("No existe ningun alumno con id " + alumnoId));
        return cursoRepository.findByAlumnosId(alumnoId,pageable).map(cursoMapper::toCursoSimpleDTO);
    }

    @Override
    public void deleteAlumno(Long id) {
        Optional<UsuarioEntity> usuario= usuarioRepository.findByAlumnoId(id);
        if(usuario.isPresent()){
            UsuarioEntity usuarioEntity = usuario.get();
            usuarioRepository.delete(usuarioEntity);
        }
        aRepository.deleteById(id);
    }
}
