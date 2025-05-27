package com.example.academia.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        System.out.println("=== JWT FILTER DEBUG ===");
        System.out.println("URL: " + request.getRequestURI());

        final String authorizationHeader = request.getHeader("Authorization");
        System.out.println("Authorization Header: " + authorizationHeader);

        String username = null;
        String jwt = null;

        // Verificar si el header Authorization contiene el token JWT
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7); // Remover "Bearer "
            System.out.println("Token extraído: " + jwt.substring(0, Math.min(20, jwt.length())) + "...");
            try {
                username = jwtUtil.extractUsername(jwt);
                System.out.println("Username extraído: " + username);
            } catch (Exception e) {
                System.err.println("Error al extraer username del token JWT: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("No hay header Authorization o no empieza con 'Bearer '");
        }

        // Si tenemos username y no hay autenticación previa
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            System.out.println("Intentando autenticar usuario: " + username);

            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                System.out.println("UserDetails cargado para: " + userDetails.getUsername());

                // Validar el token
                if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {
                    System.out.println("Token válido para: " + username);

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    System.out.println("Autenticación establecida exitosamente");
                    System.out.println("Authorities: " + userDetails.getAuthorities());
                } else {
                    System.err.println("Token inválido para usuario: " + username);
                }
            } catch (Exception e) {
                System.err.println("Error durante la autenticación: " + e.getMessage());
                e.printStackTrace();
            }
        } else if (username == null) {
            System.out.println("No se pudo extraer username del token");
        } else {
            System.out.println("Ya hay autenticación establecida");
        }

        System.out.println("=== FIN JWT FILTER DEBUG ===");
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        // No filtrar las rutas de login y registro
        return path.equals("/api/login") ||
                path.equals("/api/register") ||
                path.startsWith("/api/public");
    }
}