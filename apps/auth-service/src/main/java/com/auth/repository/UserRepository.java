package com.auth.repository;

import com.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    /**
     * Buscar usuario por email (campo principal).
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Buscar usuario por username (compatibilidad).
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Buscar usuario por email o username (para compatibilidad con login).
     */
    @Query("SELECT u FROM User u WHERE u.email = :emailOrUsername OR u.username = :emailOrUsername")
    Optional<User> findByEmailOrUsername(@Param("emailOrUsername") String emailOrUsername, @Param("emailOrUsername") String emailOrUsername2);
    
    /**
     * Verificar si existe un usuario con ese email.
     */
    boolean existsByEmail(String email);
    
    /**
     * Verificar si existe un usuario con ese username.
     */
    boolean existsByUsername(String username);
}