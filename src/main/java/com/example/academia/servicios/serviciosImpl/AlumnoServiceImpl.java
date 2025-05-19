package com.example.academia.servicios.serviciosImpl;

import com.example.academia.Exceptions.ValidationException;
import com.example.academia.entidades.AlumnoEntity;
import com.example.academia.entidades.UsuarioEntity;
import com.example.academia.repositorios.AlumnoRepository;
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

    private Pageable crearPageable(int page, int size, String sort, String direction) {
        Sort.Direction sortDirection=direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        if (sort == null || sort.isEmpty()) {
            sort = "id";  // O cualquier campo predeterminado de tu entidad
        }
        return PageRequest.of(page, size, sortDirection,sort);
    }

    @Override
    public Page<AlumnoEntity> findAll(int page, int size, String sort, String direction) {
        Pageable pageable = crearPageable(page, size, sort, direction);
        return aRepository.findAll(pageable);
    }

    @Override
    public Optional<AlumnoEntity> findById(Long id) {
        return aRepository.findById(id);
    }

    @Override
    public Page<AlumnoEntity> findByNombreOrApellido(String nombre, String apellido, int page, int size, String sort, String direction) {
       Pageable pageable = crearPageable(page, size, sort, direction);
       boolean filtraNombre = nombre != null && !nombre.trim().isEmpty();
       boolean filtraApellido = apellido != null && !apellido.trim().isEmpty();

        if (filtraNombre && filtraApellido) {
            return aRepository.findByNombreContainingIgnoreCaseAndApellidoContainingIgnoreCase(nombre, apellido, pageable);
        } else if (filtraNombre) {
            return aRepository.findByNombreContainingIgnoreCase(nombre, pageable);
        } else if (filtraApellido) {
            return aRepository.findByApellidoContainingIgnoreCase(apellido, pageable);
        } else {
            return aRepository.findAll(pageable);
        }
    }

    @Override
    public AlumnoEntity saveAlumno(AlumnoEntity alumno) {
        return aRepository.save(alumno);
    }

    @Override
    public AlumnoEntity updateAlumno(Long id, AlumnoEntity alumno, boolean syncUsuario) {
       Optional<AlumnoEntity> alumnoEncontrado = aRepository.findById(id);
       if (alumnoEncontrado.isEmpty()) {
           throw new ValidationException("No existe ningun alumno con id " + id);
       }

       alumno.setId(id);
       AlumnoEntity alumnoActual = aRepository.save(alumno);

       if (syncUsuario) {
          Optional<UsuarioEntity> usuarioEncontrado = usuarioRepository.findByAlumnoId(id);
          if (usuarioEncontrado.isPresent()) {
              UsuarioEntity usuarioActual = usuarioEncontrado.get();
              usuarioActual.setNombre(alumno.getNombre());
              usuarioActual.setApellido(alumno.getApellido());
              usuarioRepository.save(usuarioActual);
          }
       }
       return alumnoActual;
    }

    @Override
    public AlumnoEntity createAlumnoWithUser(AlumnoEntity alumno, UsuarioEntity usuario) {
       //Guardamos el Alumno primero
        AlumnoEntity alumnoActual = aRepository.save(alumno);
        //Configuramos el usuario con el alumno guardado
        usuario.setRol(UsuarioEntity.Rol.Alumno);
        usuario.setAlumno(alumnoActual);

        if (usuario.getNombre() == null || usuario.getNombre().isEmpty()) {
            usuario.setNombre(alumno.getNombre());
        }
        if (usuario.getApellido() == null || usuario.getApellido().isEmpty()) {
            usuario.setApellido(alumno.getApellido());
        }

        usuarioValidator.validateRolRelations(usuario);
        // Guardar el usuario
        usuarioRepository.save(usuario);

        return alumnoActual;
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
