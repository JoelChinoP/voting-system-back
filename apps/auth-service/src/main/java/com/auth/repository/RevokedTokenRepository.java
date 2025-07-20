package com.auth.repository;

import com.auth.model.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface RevokedTokenRepository extends JpaRepository<RevokedToken, String> {

    /**
     * Checks if a token with the given JTI exists and has not expired.
     *
     * @param jti The unique identifier of the JWT token.
     * @param expiryDate The current date.
     * @return True if the token exists and has not expired, false otherwise.
     */
    boolean existsByJtiAndExpiryDateAfter(String jti, Date expiryDate);
}
