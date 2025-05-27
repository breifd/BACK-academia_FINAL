package com.example.academia.security;

import com.example.academia.entidades.UsuarioEntity;
import com.example.academia.repositorios.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("Buscando usuario con username exacto: '" + username + "'");

        Optional<UsuarioEntity> usuario = usuarioRepository.findByUsername(username);
        System.out.println("Usuario encontrado en BD: " + (usuario.isPresent() ? "SÍ" : "NO"));

        if (usuario.isPresent()) {
            System.out.println("Hash almacenado en BD: " + usuario.get().getPassword());
        }

        return usuario.map(u -> new CustomUserDetails(u))
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado con el username: " + username));
    }
}
