package com.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {
    
    private final RedisTemplate<String, String> redisTemplate;
    private static final String BLACKLIST_PREFIX = "blacklist:";
    
    /**
     * Agrega un JTI a la blacklist
     * @param jti JWT ID del token
     * @param expirationTimeSeconds tiempo en segundos hasta que expire
     */
    public void addToBlacklist(String jti, long expirationTimeSeconds) {
        try {
            if (jti != null && !jti.trim().isEmpty() && expirationTimeSeconds > 0) {
                redisTemplate.opsForValue().set(
                    BLACKLIST_PREFIX + jti, 
                    "revoked", 
                    expirationTimeSeconds, 
                    TimeUnit.SECONDS
                );
                log.info("Token added to blacklist: JTI={}", jti);
            }
        } catch (Exception e) {
            log.error("Error adding token to blacklist (JTI: {}): {}", jti, e.getMessage());
        }
    }
    
    /**
     * Verifica si un JTI está en la blacklist
     * @param jti JWT ID del token
     * @return true si el token está revocado
     */
    public boolean isTokenRevoked(String jti) {
        try {
            if (jti == null || jti.trim().isEmpty()) {
                return false;
            }
            
            Boolean exists = redisTemplate.hasKey(BLACKLIST_PREFIX + jti);
            return exists != null && exists;
            
        } catch (Exception e) {
            log.error("Error checking token blacklist (JTI: {}): {}", jti, e.getMessage());
            return false; // En caso de error, permitir el token
        }
    }
    
    /**
     * Remueve un JTI de la blacklist
     * @param jti JWT ID del token
     */
    public void removeFromBlacklist(String jti) {
        try {
            if (jti != null && !jti.trim().isEmpty()) {
                redisTemplate.delete(BLACKLIST_PREFIX + jti);
                log.info("Token removed from blacklist: JTI={}", jti);
            }
        } catch (Exception e) {
            log.error("Error removing token from blacklist (JTI: {}): {}", jti, e.getMessage());
        }
    }
}