package com.example.academia.servicios.serviciosImpl;


import com.example.academia.DTOs.LoginResponse;
import com.example.academia.DTOs.UsuarioDTO;
import com.example.academia.Exceptions.ValidationException;
import com.example.academia.entidades.AlumnoEntity;
import com.example.academia.entidades.ProfesorEntity;
import com.example.academia.entidades.UsuarioEntity;
import com.example.academia.repositorios.AlumnoRepository;
import com.example.academia.repositorios.ProfesorRepository;
import com.example.academia.repositorios.UsuarioRepository;
import com.example.academia.servicios.UsuarioService;
import com.example.academia.validators.UsuarioValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final ProfesorRepository profesorRepository;
    private final UsuarioRepository usuarioRepository;
    private final AlumnoRepository alumnoRepository;
    private final UsuarioValidator usuarioValidator;


    @Override
    public Optional<UsuarioEntity> findByUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    @Override
    public List<UsuarioEntity> findAll() {
        return usuarioRepository.findAll();
    }

    @Override
    public LoginResponse login(String username, String password) {
        try{
            //Validamos campos
            usuarioValidator.validateUsername(username);
            usuarioValidator.validatePassword(password);

            //Buscamos el usuario por username
            Optional<UsuarioEntity> usuario = usuarioRepository.findByUsername(username);
            if(usuario.isEmpty()){
                return LoginResponse.error("Usuario o contraseña incorrectos", "INVALID_CREDENTIALS");
            }
            // Una vez encontrado el usuario vamos a comprobar las contraseñas
            UsuarioEntity usuarioEntity = usuario.get();

            if(!usuarioEntity.getPassword().equals(password)){
                return LoginResponse.error("Usuario o contraseña incorrectos", "INVALID_CREDENTIALS");
            }

            //Obtenemos los ids relacionados para saber que tipo de usuario es

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
    public UsuarioEntity saveUsuario(UsuarioEntity usuario) {
        usuarioValidator.validateRolRelations(usuario);
        return usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public UsuarioEntity createUsuario(UsuarioDTO usuarioDTO) {
        //Comprobamos si existe el usuario en la base de datos
        if(usuarioRepository.existsByUsername(usuarioDTO.getUsername())){
            throw new ValidationException("El nombre de usuario ya está en uso");
        }

        //Creamos el usuario pasandole los datos del dto (formulario)
        UsuarioEntity usuarioEntity = new UsuarioEntity();
        usuarioEntity.setUsername(usuarioDTO.getUsername());
        usuarioEntity.setPassword(usuarioDTO.getPassword());
        usuarioEntity.setNombre(usuarioDTO.getNombre());
        usuarioEntity.setApellido(usuarioDTO.getApellido());
        usuarioEntity.setRol(usuarioDTO.getRol());

        //Asignar relaciones que corresponda
        if(usuarioDTO.getRol() == UsuarioEntity.Rol.Profesor && usuarioDTO.getProfesorId() != null){
            Optional<ProfesorEntity> profesor = profesorRepository.findById(usuarioDTO.getProfesorId());
            if(profesor.isEmpty()){
                throw new ValidationException("El profesor con ID "+usuarioDTO.getProfesorId()+" no existe");
            }
            Optional<UsuarioEntity> usuarioExistente= usuarioRepository.findByProfesorId(usuarioDTO.getProfesorId());
            if(usuarioExistente.isPresent()){
                throw new ValidationException("El profesor con ID "+usuarioDTO.getProfesorId()+" ya está asociado al usuario con ID: "+usuarioDTO.getProfesorId());
            }
            usuarioEntity.setProfesor(profesor.get());
        }

        if (usuarioDTO.getRol() == UsuarioEntity.Rol.Alumno && usuarioDTO.getAlumnoId() != null) {
            Optional<AlumnoEntity> alumno = alumnoRepository.findById(usuarioDTO.getAlumnoId());
            if (alumno.isEmpty()) {
                throw new ValidationException("El alumno con ID " + usuarioDTO.getAlumnoId() + " no existe");
            }

            Optional<UsuarioEntity> usuarioExistente = usuarioRepository.findByAlumnoId(usuarioDTO.getAlumnoId());
            if (usuarioExistente.isPresent()) {
                throw new ValidationException("El alumno con ID " + usuarioDTO.getAlumnoId() +
                        " ya está asociado al usuario " + usuarioExistente.get().getUsername());
            }

            usuarioEntity.setAlumno(alumno.get());
        }
        usuarioValidator.validateRolRelations(usuarioEntity);

        return usuarioRepository.save(usuarioEntity);
    }

    @Override
    @Transactional
    public UsuarioEntity updateUsuario(Long id, UsuarioDTO usuarioDTO) {
        Optional<UsuarioEntity> usuario = usuarioRepository.findById(id);
        if(usuario.isEmpty()){
            throw new ValidationException("El usuario con ID " + id + " no existe");
        }
        UsuarioEntity usuarioEntity = usuario.get();
        //No se permite cambiar el username si ya existe
        //Si el username del usuario es distinto al nuevo que vas a introdcuir pero existe un usuario con ese username
        if (!usuarioEntity.getUsername().equals(usuarioDTO.getUsername()) && usuarioRepository.existsByUsername(usuarioDTO.getUsername())) {
            throw new ValidationException("El nombre de usuario " + usuarioDTO.getUsername() + " ya está en uso");
        }
        usuarioEntity.setUsername(usuarioDTO.getUsername());
        if (usuarioDTO.getPassword() != null && !usuarioDTO.getPassword().isEmpty()) {
            usuarioEntity.setPassword(usuarioDTO.getPassword());
        }
        usuarioEntity.setNombre(usuarioDTO.getNombre());
        usuarioEntity.setApellido(usuarioDTO.getApellido());

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
            Optional<ProfesorEntity> profesorOpt = profesorRepository.findById(usuarioDTO.getProfesorId());
            if (profesorOpt.isEmpty()) {
                throw new ValidationException("El profesor con ID " + usuarioDTO.getProfesorId() + " no existe");
            }

            // Verificar que el profesor no esté ya asociado a otro usuario
            Optional<UsuarioEntity> usuarioExistente = usuarioRepository.findByProfesorId(usuarioDTO.getProfesorId());
            if (usuarioExistente.isPresent() && !usuarioExistente.get().getId().equals(id)) {
                throw new ValidationException("El profesor con ID " + usuarioDTO.getProfesorId() +
                        " ya está asociado al usuario " + usuarioExistente.get().getUsername());
            }

            usuarioEntity.setProfesor(profesorOpt.get());
        } else if (usuarioDTO.getRol() == UsuarioEntity.Rol.Profesor) {
            // Si tiene rol profesor pero no se especifica ID, mantener la relación anterior
        } else {
            usuarioEntity.setProfesor(null);
        }

        // Actualizar relación con Alumno si corresponde
        if (usuarioDTO.getRol() == UsuarioEntity.Rol.Alumno && usuarioDTO.getAlumnoId() != null) {
            Optional<AlumnoEntity> alumnoOpt = alumnoRepository.findById(usuarioDTO.getAlumnoId());
            if (alumnoOpt.isEmpty()) {
                throw new ValidationException("El alumno con ID " + usuarioDTO.getAlumnoId() + " no existe");
            }

            // Verificar que el alumno no esté ya asociado a otro usuario
            Optional<UsuarioEntity> usuarioExistente = usuarioRepository.findByAlumnoId(usuarioDTO.getAlumnoId());
            if (usuarioExistente.isPresent() && !usuarioExistente.get().getId().equals(id)) {
                throw new ValidationException("El alumno con ID " + usuarioDTO.getAlumnoId() +
                        " ya está asociado al usuario " + usuarioExistente.get().getUsername());
            }

            usuarioEntity.setAlumno(alumnoOpt.get());
        } else if (usuarioDTO.getRol() == UsuarioEntity.Rol.Alumno) {
            // Si tiene rol alumno pero no se especifica ID, mantener la relación anterior
        } else {
            usuarioEntity.setAlumno(null);
        }

        // Validar relaciones según el rol
        usuarioValidator.validateRolRelations(usuarioEntity);

        return usuarioRepository.save(usuarioEntity);
    }

    @Override
    @Transactional
    public void deleteUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }

    @Override
    public List<UsuarioEntity> findByRol(UsuarioEntity.Rol rol) {
        return usuarioRepository.findByRol(rol);
    }

    @Override
    public Optional<UsuarioEntity> findByProfesorId(Long profesorId) {
        return usuarioRepository.findByProfesorId(profesorId);
    }

    @Override
    public Optional<UsuarioEntity> findByAlumnoId(Long alumnoId) {
        return usuarioRepository.findByAlumnoId(alumnoId);
    }

    // Syncronizamos para que el usuario tenga el mismo nombre que el profesor o alumno
    @Override
    @Transactional
    public UsuarioEntity syncNameWithRelatedEntity(Long usuarioId) {
        Optional<UsuarioEntity> usuario = usuarioRepository.findById(usuarioId);
        if(usuario.isEmpty()){
            throw new ValidationException("El usuario con ID " + usuarioId + " no existe");
        }
        UsuarioEntity usuarioEntity = usuario.get();
        if(usuarioEntity.getRol() == UsuarioEntity.Rol.Profesor && usuarioEntity.getProfesor() !=null){
            ProfesorEntity profesor= usuarioEntity.getProfesor();
            usuarioEntity.setNombre(profesor.getNombre());
            usuarioEntity.setApellido(profesor.getApellido());
        }else if (usuarioEntity.getRol() == UsuarioEntity.Rol.Alumno && usuarioEntity.getAlumno() != null) {
            AlumnoEntity alumno = usuarioEntity.getAlumno();
            usuarioEntity.setNombre(alumno.getNombre());
            usuarioEntity.setApellido(alumno.getApellido());
        }
        return usuarioRepository.save(usuarioEntity);
    }
}
