package com.auth.repository;

import com.auth.model.RefreshToken;
import com.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    
    @Query("SELECT rt FROM RefreshToken rt " +
           "WHERE rt.token = :token " +
           "AND rt.revoked = false " +
           "AND rt.expiryDate > :now")
    Optional<RefreshToken> findValidTokenByToken(@Param("token") String token, 
                                                @Param("now") LocalDateTime now);
    
    @Query("SELECT rt FROM RefreshToken rt " +
           "WHERE rt.user = :user " +
           "AND rt.revoked = false " +
           "AND rt.expiryDate > :now " +
           "ORDER BY rt.createdAt DESC")
    List<RefreshToken> findActiveTokensByUser(@Param("user") User user, 
                                            @Param("now") LocalDateTime now);
    
    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.revoked = true, rt.updatedAt = :now " +
           "WHERE rt.user = :user AND rt.revoked = false")
    int revokeAllByUser(@Param("user") User user, @Param("now") LocalDateTime now);
    
    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.revoked = true, rt.updatedAt = :now " +
           "WHERE rt.token = :token")
    int revokeByToken(@Param("token") String token, @Param("now") LocalDateTime now);
    
    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.revoked = true, rt.updatedAt = :now " +
           "WHERE rt.expiryDate < :now AND rt.revoked = false")
    int revokeExpiredTokens(@Param("now") LocalDateTime now);
    
    @Query("SELECT COUNT(rt) FROM RefreshToken rt " +
           "WHERE rt.user = :user " +
           "AND rt.revoked = false " +
           "AND rt.expiryDate > :now")
    long countActiveTokensByUser(@Param("user") User user, @Param("now") LocalDateTime now);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken rt " +
           "WHERE rt.revoked = true " +
           "AND rt.expiryDate < :cutoffDate")
    int deleteOldRevokedTokens(@Param("cutoffDate") LocalDateTime cutoffDate);
}