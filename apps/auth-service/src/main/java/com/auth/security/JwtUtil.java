package com.auth.security;

import com.auth.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import java.util.UUID;

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}") // 24 horas por defecto
    private Long jwtExpiration;

    // Claims específicos para el sistema de votación
    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_HAS_VOTED = "hasVoted";

    /**
     * Genera un token JWT con payload personalizado para el usuario
     */
    public String generateTokenWithPayload(User user, boolean hasVoted) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_USER_ID, user.getId().toString());
        claims.put(CLAIM_EMAIL, user.getUsername());
        claims.put(CLAIM_ROLE, user.getRoles() != null ? user.getRoles() : "USER");
        claims.put(CLAIM_HAS_VOTED, hasVoted);

        return createToken(claims, user.getUsername());
    }

    /**
     * Genera un token JWT estándar
     */
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    /**
     * Crea el token JWT con claims y subject
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        // Generar JTI único
        String jti = UUID.randomUUID().toString();

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .setId(jti) // Agregar JTI al token
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extrae el JTI (JWT ID) del token
     * 
     * @param token JWT token
     * @return JTI claim como String, o null si no está presente
     */
    public String extractJti(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.getId(); // El JTI se obtiene con getId()
        } catch (Exception e) {
            log.error("Error extracting JTI from token: {}", e.getMessage());
            return null; // Retorna null si no se puede extraer
        }
    }

    /**
     * Verifica si el token tiene un JTI válido
     * 
     * @param token JWT token
     * @return true si el token tiene JTI
     */
    public boolean hasValidJti(String token) {
        try {
            String jti = extractJti(token);
            return jti != null && !jti.trim().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extrae el username (subject) del token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrae la fecha de expiración del token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrae un claim específico del token
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extrae todos los claims del token
     */
    public Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            log.error("JWT token is malformed: {}", e.getMessage());
            throw e;
        } catch (SecurityException e) {
            log.error("JWT signature validation failed: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("JWT token compact is empty: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Extrae el payload completo del token como Map
     */
    public Map<String, Object> extractPayload(String token) {
        try {
            Claims claims = extractAllClaims(token);

            Map<String, Object> payload = new HashMap<>();
            payload.put(CLAIM_USER_ID, claims.get(CLAIM_USER_ID));
            payload.put(CLAIM_EMAIL, claims.get(CLAIM_EMAIL));
            payload.put(CLAIM_ROLE, claims.get(CLAIM_ROLE));
            payload.put(CLAIM_HAS_VOTED, claims.get(CLAIM_HAS_VOTED, Boolean.class));
            payload.put("sub", claims.getSubject());
            payload.put("iat", claims.getIssuedAt());
            payload.put("exp", claims.getExpiration());
            payload.put("jti", claims.getId()); // Agregar JTI al payload

            return payload;
        } catch (Exception e) {
            log.error("Error extracting payload from token: {}", e.getMessage());
            throw new RuntimeException("Invalid token", e);
        }
    }

    /**
     * MÉTODO REQUERIDO: Extrae el tiempo de expiración del token en milisegundos
     */
    public long getExpirationTime(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Date expiration = claims.getExpiration();
            return expiration.getTime();
        } catch (Exception e) {
            log.error("Error extracting expiration time from token: {}", e.getMessage());
            throw new RuntimeException("Invalid token", e);
        }
    }

    /**
     * Verifica si el token ha expirado
     */
    public boolean isTokenExpired(String token) {
        try {
            return getExpirationTime(token) < System.currentTimeMillis();
        } catch (Exception e) {
            return true; // Si hay error, consideramos que está expirado
        }
    }

    /**
     * Obtiene la fecha de expiración del token como Date
     */
    public Date getExpirationDate(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.getExpiration();
        } catch (Exception e) {
            log.error("Error extracting expiration date from token: {}", e.getMessage());
            throw new RuntimeException("Invalid token", e);
        }
    }

    /**
     * Valida el token JWT
     */
    public boolean validateToken(String token, String username) {
        try {
            final String extractedUsername = extractUsername(token);
            return (extractedUsername.equals(username) && !isTokenExpired(token));
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Valida el token JWT sin username
     */
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene la clave de firma para JWT
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extrae el ID del usuario del token
     */
    public String extractUserId(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.get(CLAIM_USER_ID, String.class);
        } catch (Exception e) {
            log.error("Error extracting user ID from token: {}", e.getMessage());
            throw new RuntimeException("Invalid token", e);
        }
    }

    /**
     * Extrae el rol del usuario del token
     */
    public String extractUserRole(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.get(CLAIM_ROLE, String.class);
        } catch (Exception e) {
            log.error("Error extracting user role from token: {}", e.getMessage());
            throw new RuntimeException("Invalid token", e);
        }
    }

    /**
     * Extrae el estado de votación del token
     */
    public Boolean extractHasVoted(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.get(CLAIM_HAS_VOTED, Boolean.class);
        } catch (Exception e) {
            log.error("Error extracting hasVoted from token: {}", e.getMessage());
            throw new RuntimeException("Invalid token", e);
        }
    }

    /**
     * Obtiene el tiempo restante antes de la expiración en segundos
     */
    public long getTimeUntilExpiration(String token) {
        try {
            long expirationTime = getExpirationTime(token);
            long currentTime = System.currentTimeMillis();
            return Math.max(0, (expirationTime - currentTime) / 1000);
        } catch (Exception e) {
            return 0;
        }
    }
}