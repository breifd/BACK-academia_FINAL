package com.example.academia.security;

import com.example.academia.entidades.UsuarioEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final UsuarioEntity usuario;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        System.out.println("Generando autoridades para usuario: " + usuario.getUsername());
        System.out.println("Rol del usuario: " + usuario.getRol().name());
        // Convertir el rol del usuario a GrantedAuthority

        Collection<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name())
        );

        System.out.println("Autoridades generadas: " + authorities);
        return authorities;
    }

    @Override
    public String getPassword() {
        return usuario.getPassword();
    }

    @Override
    public String getUsername() {
        return usuario.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // Método para obtener el usuario completo
    public UsuarioEntity getUsuario() {
        return usuario;
    }

    // Métodos de conveniencia
    public String getRol() {
        return usuario.getRol().name();
    }

    public Long getProfesorId() {
        return usuario.getProfesor() != null ? usuario.getProfesor().getId() : null;
    }

    public Long getAlumnoId() {
        return usuario.getAlumno() != null ? usuario.getAlumno().getId() : null;
    }
}