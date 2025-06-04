package com.example.academia.DTOs;

import com.example.academia.entidades.UsuarioEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Objeto de transferencia de datos (DTO) para la respuesta del login
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private boolean success;
    private String message;
    private Long id; // ✅ NUEVO: ID del usuario
    private String username;
    private String nombre;
    private String apellido;
    private UsuarioEntity.Rol rol;
    private Long profesorId;
    private Long alumnoId;
    private String errorCode;

    private String token;
    private String tokenType = "Bearer";

    // ✅ ACTUALIZADO: Respuesta para el Login Exitoso CON TOKEN JWT
    public static LoginResponse success(String username, String nombre, String apellido,
                                        UsuarioEntity.Rol rol, Long profesorId, Long alumnoId, String token) {
        return LoginResponse.builder()
                .success(true)
                .message("Login exitoso")
                .username(username)
                .nombre(nombre)
                .apellido(apellido)
                .rol(rol)
                .profesorId(profesorId)
                .alumnoId(alumnoId)
                .token(token)
                .tokenType("Bearer")
                .build();
    }

    // ✅ NUEVO: Respuesta exitosa con ID de usuario
    public static LoginResponse success(Long id, String username, String nombre, String apellido,
                                        UsuarioEntity.Rol rol, Long profesorId, Long alumnoId, String token) {
        return LoginResponse.builder()
                .success(true)
                .message("Login exitoso")
                .id(id)
                .username(username)
                .nombre(nombre)
                .apellido(apellido)
                .rol(rol)
                .profesorId(profesorId)
                .alumnoId(alumnoId)
                .token(token)
                .tokenType("Bearer")
                .build();
    }

    // ✅ ACTUALIZADO: Respuesta para el Login Exitoso SIN TOKEN
    public static LoginResponse success(String username, String nombre, String apellido,
                                        UsuarioEntity.Rol rol, Long profesorId, Long alumnoId) {
        return LoginResponse.builder()
                .success(true)
                .message("Login exitoso")
                .username(username)
                .nombre(nombre)
                .apellido(apellido)
                .rol(rol)
                .profesorId(profesorId)
                .alumnoId(alumnoId)
                .build();
    }

    // ✅ NUEVO: Respuesta exitosa con ID pero sin token
    public static LoginResponse success(Long id, String username, String nombre, String apellido,
                                        UsuarioEntity.Rol rol, Long profesorId, Long alumnoId) {
        return LoginResponse.builder()
                .success(true)
                .message("Login exitoso")
                .id(id)
                .username(username)
                .nombre(nombre)
                .apellido(apellido)
                .rol(rol)
                .profesorId(profesorId)
                .alumnoId(alumnoId)
                .build();
    }

    // Respuesta de login fallido
    public static LoginResponse error(String message, String errorCode) {
        return LoginResponse.builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .build();
    }
}