package com.auth.service;

import com.auth.dto.LoginRequest;
import com.auth.dto.RegisterRequest;
import com.auth.model.User;
import com.auth.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Constructor único: Spring lo inyecta automáticamente
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * RF-001: Registro de nuevos usuarios.
     * Usa email como campo principal y lo duplica en username para compatibilidad.
     */
    public User register(RegisterRequest request) {
        // Verificar si el usuario ya existe por email
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DataIntegrityViolationException("El email ya está registrado");
        }

        // Crear nuevo usuario
        User user = User.builder()
                .username(request.getEmail())  // Usar email como username para compatibilidad
                .email(request.getEmail())     // Campo email principal
                .fullName(request.getFullName())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of("USER"))
                .isActive(true)
                .isEligible(true)
                .hasVoted(false)
                .createdAt(LocalDateTime.now())
                .build();

        // Guardar y devolver el usuario
        return userRepository.save(user);
    }

    /**
     * RF-002: Autenticación de usuarios por email.
     */
    public User authenticate(LoginRequest request) {
        // Buscar usuario por email
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        
        if (userOpt.isEmpty()) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        User user = userOpt.get();

        // Verificar que el usuario esté activo
        if (!user.isActive()) {
            throw new BadCredentialsException("Usuario inactivo");
        }

        // Verificar contraseña
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        return user;
    }

    /**
     * Buscar usuario por email.
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Verificar si un email ya existe.
     */
    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
}