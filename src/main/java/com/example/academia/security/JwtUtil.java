package com.example.academia.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {
    //Generaremos y Validaremos los Tokens para poder identificar al usuario
    // Clave secreta para firmar los tokens (debe ser segura en producción)
    private final String SECRET_KEY = "mySecretKeyForJWTTokenGenerationAcademiaApp2025SecureKey";

    // Tiempo de expiración del token (24 horas)
    @Value("${jwt.expiration:86400000}")
    private Long jwtExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    // Generar token para un usuario
    public String generateToken(String username, String rol, Long profesorId, Long alumnoId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("rol", rol);
        if (profesorId != null) {
            claims.put("profesorId", profesorId);
        }
        if (alumnoId != null) {
            claims.put("alumnoId", alumnoId);
        }
        return createToken(claims, username);
    }

    // Crear el token JWT
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Extraer username del token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extraer fecha de expiración
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Extraer rol del token
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("rol", String.class));
    }

    // Extraer profesor ID del token
    public Long extractProfesorId(String token) {
        return extractClaim(token, claims -> {
            Object profesorId = claims.get("profesorId");
            return profesorId != null ? Long.valueOf(profesorId.toString()) : null;
        });
    }

    // Extraer alumno ID del token
    public Long extractAlumnoId(String token) {
        return extractClaim(token, claims -> {
            Object alumnoId = claims.get("alumnoId");
            return alumnoId != null ? Long.valueOf(alumnoId.toString()) : null;
        });
    }

    // Extraer claim específico
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Extraer todos los claims del token
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            throw new RuntimeException("Token JWT inválido", e);
        }
    }

    // Verificar si el token ha expirado
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Validar token
    public Boolean validateToken(String token, String username) {
        try {
            final String extractedUsername = extractUsername(token);
            return (extractedUsername.equals(username) && !isTokenExpired(token));
        } catch (Exception e) {
            return false;
        }
    }

    // Validar token sin comparar username
    public Boolean validateToken(String token) {
        try {
            extractAllClaims(token); // Esto lanzará excepción si el token es inválido
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

}
