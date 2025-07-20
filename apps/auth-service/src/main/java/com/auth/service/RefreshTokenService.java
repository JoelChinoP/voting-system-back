package com.auth.service;

import com.auth.model.RefreshToken;
import com.auth.model.User;
import com.auth.repository.RefreshTokenRepository;
import com.auth.exception.TokenException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {
    
    private final RefreshTokenRepository refreshTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();
    
    @Value("${jwt.refresh-expiration:604800000}")
    private Long refreshTokenDurationMs;
    
    @Value("${auth.max-refresh-tokens-per-user:5}")
    private int maxTokensPerUser;
    
    @Value("${auth.enable-device-tracking:true}")
    private boolean enableDeviceTracking;
    
    @Transactional
    public RefreshToken createRefreshToken(User user, String deviceInfo, String ipAddress) {
        try {
            long activeTokenCount = refreshTokenRepository.countActiveTokensByUser(user, LocalDateTime.now());
            if (activeTokenCount >= maxTokensPerUser) {
                log.warn("User {} has {} active tokens, revoking oldest", user.getUsername(), activeTokenCount);
                revokeOldestTokensForUser(user, maxTokensPerUser - 1);
            }
            
            String token = generateSecureToken();
            LocalDateTime expiryDate = LocalDateTime.now().plusSeconds(refreshTokenDurationMs / 1000);
            
            RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .user(user)
                .expiryDate(expiryDate)
                .deviceInfo(enableDeviceTracking ? deviceInfo : null)
                .ipAddress(ipAddress)
                .revoked(false)
                .build();
            
            RefreshToken saved = refreshTokenRepository.save(refreshToken);
            
            log.info("Created refresh token for user: {} | tokenId: {} | expiresAt: {} | ip: {}", 
                user.getUsername(), saved.getId(), expiryDate, ipAddress);
            
            return saved;
            
        } catch (Exception e) {
            log.error("Error creating refresh token for user {}: {}", user.getUsername(), e.getMessage(), e);
            throw new TokenException("Failed to create refresh token", e);
        }
    }
    
    public RefreshToken verifyRefreshToken(String token) {
        try {
            Optional<RefreshToken> optionalToken = refreshTokenRepository
                .findValidTokenByToken(token, LocalDateTime.now());
            
            if (optionalToken.isEmpty()) {
                log.warn("Invalid or expired refresh token attempted: {}", 
                    token.substring(0, Math.min(token.length(), 10)) + "...");
                throw new TokenException("Refresh token is invalid or expired");
            }
            
            RefreshToken refreshToken = optionalToken.get();
            refreshToken.markAsUsed();
            refreshTokenRepository.save(refreshToken);
            
            log.info("Refresh token verified successfully | tokenId: {} | user: {} | lastUsed: {}", 
                refreshToken.getId(), refreshToken.getUser().getUsername(), refreshToken.getLastUsedAt());
            
            return refreshToken;
            
        } catch (TokenException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error verifying refresh token: {}", e.getMessage(), e);
            throw new TokenException("Failed to verify refresh token", e);
        }
    }
    
    @Transactional
    public void revokeRefreshToken(String token) {
        try {
            int revokedCount = refreshTokenRepository.revokeByToken(token, LocalDateTime.now());
            
            if (revokedCount > 0) {
                log.info("Refresh token revoked successfully: {}", 
                    token.substring(0, Math.min(token.length(), 10)) + "...");
            } else {
                log.warn("Attempted to revoke non-existent token: {}", 
                    token.substring(0, Math.min(token.length(), 10)) + "...");
            }
            
        } catch (Exception e) {
            log.error("Error revoking refresh token: {}", e.getMessage(), e);
            throw new TokenException("Failed to revoke refresh token", e);
        }
    }
    
    @Transactional
    public void revokeAllUserTokens(User user) {
        try {
            int revokedCount = refreshTokenRepository.revokeAllByUser(user, LocalDateTime.now());
            log.info("Revoked {} refresh tokens for user: {}", revokedCount, user.getUsername());
            
        } catch (Exception e) {
            log.error("Error revoking all tokens for user {}: {}", user.getUsername(), e.getMessage(), e);
            throw new TokenException("Failed to revoke user tokens", e);
        }
    }
    
    @Transactional
    public RefreshToken rotateRefreshToken(RefreshToken currentToken, String deviceInfo, String ipAddress) {
        try {
            currentToken.setRevoked(true);
            currentToken.setUpdatedAt(LocalDateTime.now());
            refreshTokenRepository.save(currentToken);
            
            RefreshToken newToken = createRefreshToken(currentToken.getUser(), deviceInfo, ipAddress);
            
            log.info("Rotated refresh token for user: {} | oldTokenId: {} | newTokenId: {}", 
                currentToken.getUser().getUsername(), currentToken.getId(), newToken.getId());
            
            return newToken;
            
        } catch (Exception e) {
            log.error("Error rotating refresh token: {}", e.getMessage(), e);
            throw new TokenException("Failed to rotate refresh token", e);
        }
    }
    
    private String generateSecureToken() {
        byte[] randomBytes = new byte[64];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
    
    @Transactional
    protected void revokeOldestTokensForUser(User user, int keepCount) {
        try {
            List<RefreshToken> activeTokens = refreshTokenRepository
                .findActiveTokensByUser(user, LocalDateTime.now());
            
            if (activeTokens.size() > keepCount) {
                List<RefreshToken> tokensToRevoke = activeTokens.subList(keepCount, activeTokens.size());
                
                for (RefreshToken token : tokensToRevoke) {
                    token.setRevoked(true);
                    token.setUpdatedAt(LocalDateTime.now());
                }
                
                refreshTokenRepository.saveAll(tokensToRevoke);
                log.info("Revoked {} oldest tokens for user: {}", tokensToRevoke.size(), user.getUsername());
            }
            
        } catch (Exception e) {
            log.error("Error revoking oldest tokens for user {}: {}", user.getUsername(), e.getMessage(), e);
        }
    }
    
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void cleanupExpiredTokens() {
        try {
            LocalDateTime now = LocalDateTime.now();
            int revokedCount = refreshTokenRepository.revokeExpiredTokens(now);
            LocalDateTime cutoffDate = now.minusDays(30);
            int deletedCount = refreshTokenRepository.deleteOldRevokedTokens(cutoffDate);
            
            if (revokedCount > 0 || deletedCount > 0) {
                log.info("Cleanup completed | revoked: {} | deleted: {}", revokedCount, deletedCount);
            }
            
        } catch (Exception e) {
            log.error("Error during token cleanup: {}", e.getMessage(), e);
        }
    }
}