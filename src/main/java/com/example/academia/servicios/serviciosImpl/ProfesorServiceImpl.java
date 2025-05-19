package com.example.academia.servicios.serviciosImpl;

import com.example.academia.Exceptions.ValidationException;
import com.example.academia.entidades.ProfesorEntity;
import com.example.academia.entidades.UsuarioEntity;
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

@Service
@RequiredArgsConstructor
public  class ProfesorServiceImpl implements ProfesorService {

    private final ProfesorRepository profesorRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioValidator usuarioValidator;


    private Pageable createPageable(int page, int size, String sort, String direction) {
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        if (sort == null || sort.isEmpty()) {
            sort = "id";  // O cualquier campo predeterminado de tu entidad
        }
        return PageRequest.of(page, size, sortDirection, sort);
    }

    @Override
    public Page<ProfesorEntity> findAll(int page, int size, String sort, String direction) {
        Pageable pageable = createPageable(page, size, sort, direction);
        return profesorRepository.findAll(pageable);
    }

    @Override
    public Optional<ProfesorEntity> findById(Long id) {
        return profesorRepository.findById(id);
    }

    @Override
    public List<ProfesorEntity> findAllLista() {
        return profesorRepository.findAll();
    }

    @Override
    public Page<ProfesorEntity> findByNombreOrApellido(String nombre, String apellido, int page, int size, String sort, String direction) {
        Pageable pageable = createPageable(page, size, sort, direction);
        boolean filtraNombre = nombre != null && !nombre.trim().isEmpty();
        boolean filtraApellido = apellido != null && !apellido.trim().isEmpty();
        if (filtraNombre && filtraApellido) {
            return profesorRepository.findByNombreContainingIgnoreCaseAndApellidoContainingIgnoreCase(nombre, apellido, pageable);
        } else if (filtraNombre) {
            return profesorRepository.findByNombreContainingIgnoreCase(nombre, pageable);
        } else if (filtraApellido) {
            return profesorRepository.findByApellidoContainingIgnoreCase(apellido, pageable);
        } else {
            return profesorRepository.findAll(pageable);
        }
    }


    @Override
    public Page<ProfesorEntity> findByEspecialidad(String especialidad, int page, int size, String sort, String direction) {
        Pageable pageable = createPageable(page, size, sort, direction);
        return profesorRepository.findByEspecialidadIgnoreCase(especialidad, pageable);
    }

    @Override
    public ProfesorEntity updateProfesor(Long id, ProfesorEntity profesor, boolean syncUsuario) {
        Optional<ProfesorEntity> profesorEntity = profesorRepository.findById(id);
        if (profesorEntity.isEmpty()) {
            throw new ValidationException("El profesor con ID " + id + " no existe.");
        }
        //Establecer el ID en el objeto para actualizarlo
        profesor.setId(id);
        ProfesorEntity profesorActualizado = profesorRepository.save(profesor);

        //Si se solicita sincronizacion, actualizar el usuario
        if(syncUsuario) {
            Optional<UsuarioEntity> usuario = usuarioRepository.findByProfesorId(id);
            if (usuario.isPresent()) {
                UsuarioEntity usuarioActualizado = usuario.get();
                usuarioActualizado.setNombre(profesorActualizado.getNombre());
                usuarioActualizado.setApellido(profesorActualizado.getApellido());
                usuarioRepository.save(usuarioActualizado);
            }
        }
        return profesorActualizado;
    }

    @Override
    public ProfesorEntity createProfesorWithUser(ProfesorEntity profesor, UsuarioEntity usuario) {
        //Verificar si existe el nombre de usuario
        if(usuarioRepository.existsByUsername(usuario.getUsername())) {
            throw new ValidationException("Ya existe un usuario con ese username. "+usuario.getUsername());
        }
        // Guardar profesor primero
        ProfesorEntity savedProfesor = profesorRepository.save(profesor);
        try {
            // Configurar el usuario con el profesor guardado
            usuario.setRol(UsuarioEntity.Rol.Profesor);
            usuario.setProfesor(savedProfesor);

            // Usar los datos del profesor para el usuario si no se especifican
            if (usuario.getNombre() == null || usuario.getNombre().isEmpty()) {
                usuario.setNombre(profesor.getNombre());
            }
            if (usuario.getApellido() == null || usuario.getApellido().isEmpty()) {
                usuario.setApellido(profesor.getApellido());
            }

            // Validar relaciones
            usuarioValidator.validateRolRelations(usuario);

            // Guardar el usuario
            usuarioRepository.save(usuario);

            return savedProfesor;
        }catch (Exception e) {
            throw new ValidationException("Error al crear el usuario asociado: "+e.getMessage());
        }
    }


    @Override
    public ProfesorEntity saveProfesor(ProfesorEntity profesor) {
        return profesorRepository.save(profesor);
    }

    @Override
    public void deleteProfesor(Long id) {
        //Primero miramos si está relacionado con el usuario y eliminamos la relacion y guardamosl el usuario
        Optional<UsuarioEntity> usuarioOpt = usuarioRepository.findByProfesorId(id);
        if (usuarioOpt.isPresent()) {
            UsuarioEntity usuario = usuarioOpt.get();
            usuario.setProfesor(null);
            usuarioRepository.save(usuario);
        }

        profesorRepository.deleteById(id);
    }
}
