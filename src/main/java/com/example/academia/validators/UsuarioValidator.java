package com.example.academia.validators;

import com.example.academia.DTOs.Created.AlumnoCreateDTO;
import com.example.academia.DTOs.Created.ProfesorCreateDTO;
import com.example.academia.DTOs.Created.UsuarioCreateDTO;
import com.example.academia.entidades.UsuarioEntity;
import jakarta.validation.ValidationException;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.CompositeTypeRegistration;
import org.springframework.stereotype.Component;

@Component
public class UsuarioValidator {

    //Validamos segun el rol del usuario
    public void validateRolRelations(UsuarioEntity a){
        if(a.getRol() == UsuarioEntity.Rol.Admin && (a.getProfesor()!=null || a.getAlumno()!=null)){
            throw new ValidationException("Un usuario ADMIN no puede ser ni alumno ni profesor");
        }
        if(a.getRol() == UsuarioEntity.Rol.Profesor && a.getProfesor()==null){
            throw new ValidationException("El usuario con rol PROFESOR tiene que estar asociado con un profesor");
        }
        if(a.getRol() == UsuarioEntity.Rol.Alumno && a.getAlumno()==null){
            throw new ValidationException("El usuario con rol ALUMNO tiene que estar asociado con un alumno");
        }
    }
    //Validamos UserName que no sea vacio o nulo
    public void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("El nombre de usuario es obligatorio");
        }
    }
    //Validamos que la contraseña no se vacía o nula
    public void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new ValidationException("La contraseña es obligatorio");
        }
    }
    public void validateRequiredUser(Object profileDTO, String profileType) {
        if (profileDTO instanceof AlumnoCreateDTO) {
            AlumnoCreateDTO alumno = (AlumnoCreateDTO) profileDTO;
            if (alumno.getUsuario() == null) {
                throw new ValidationException("Todo " + profileType + " debe tener un usuario asociado");
            }
            validateUserData(alumno.getUsuario());
        } else if (profileDTO instanceof ProfesorCreateDTO) {
            ProfesorCreateDTO profesor = (ProfesorCreateDTO) profileDTO;
            if (profesor.getUsuario() == null) {
                throw new ValidationException("Todo " + profileType + " debe tener un usuario asociado");
            }
            validateUserData(profesor.getUsuario());
        }
    }
    private void validateUserData(UsuarioCreateDTO usuario) {
        if (usuario.getUsername() == null || usuario.getUsername().trim().isEmpty()) {
            throw new ValidationException("El username es obligatorio");
        }
        if (usuario.getPassword() == null || usuario.getPassword().trim().isEmpty()) {
            throw new ValidationException("La contraseña es obligatoria");
        }
    }
}
