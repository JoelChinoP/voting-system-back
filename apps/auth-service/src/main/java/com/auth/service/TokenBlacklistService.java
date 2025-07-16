package com.auth.service;

import com.auth.model.RevokedToken;
import com.auth.repository.RevokedTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RevokedTokenRepository revokedTokenRepository;

    /**
     * Invalidates a JWT token by storing its JTI in the blacklist.
     *
     * @param jti The unique identifier of the JWT token.
     * @param expiryDate The expiration date of the token.
     */
    public void invalidateToken(String jti, Date expiryDate) {
        RevokedToken revokedToken = new RevokedToken();
        revokedToken.setJti(jti);
        revokedToken.setExpiryDate(expiryDate);
        revokedTokenRepository.save(revokedToken);
    }

    /**
     * Checks if a JWT token is revoked.
     *
     * @param jti The unique identifier of the JWT token.
     * @return True if the token is revoked, false otherwise.
     */
    public boolean isTokenRevoked(String jti) {
        return revokedTokenRepository.existsByJtiAndExpiryDateAfter(jti, Date.from(Instant.now()));
    }
}
