package com.example.academia.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(authz -> authz
                                // ===== MODO DESARROLLO: TODAS LAS RUTAS PÚBLICAS =====
                                .requestMatchers("/api/**").permitAll()

                                // Permitir todo lo demás también
                                .anyRequest().permitAll()

                        // ===== COMENTADO: Configuración de seguridad real =====
                        // Descomenta esto cuando quieras aplicar seguridad real:

                        // // Rutas públicas sin autenticación
                        // .requestMatchers("/api/login", "/api/register").permitAll()
                        // .requestMatchers("/api/public/**").permitAll()
                        // .requestMatchers("/api/check-username/**").permitAll()
                        //
                        // // Rutas que requieren solo autenticación
                        // .requestMatchers("/api/profesores/**").authenticated()
                        // .requestMatchers("/api/alumnos/**").authenticated()
                        // .requestMatchers("/api/cursos/**").authenticated()
                        // .requestMatchers("/api/tareas/**").authenticated()
                        // .requestMatchers("/api/entregas/**").authenticated()
                        // .requestMatchers("/api/me").authenticated()
                        // .requestMatchers("/api/refresh-token").authenticated()
                        //
                        // // Rutas que requieren rol específico
                        // .requestMatchers("/api/usuarios/**").hasRole("Admin")
                        //
                        // // Cualquier otra ruta requiere autenticación
                        // .requestMatchers("/api/**").authenticated()
                        // .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Orígenes permitidos
        configuration.setAllowedOriginPatterns(Arrays.asList("http://localhost:*", "https://localhost:*"));

        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));

        // Headers permitidos
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // Permitir credenciales
        configuration.setAllowCredentials(true);

        // Headers expuestos
        configuration.setExposedHeaders(Arrays.asList("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}