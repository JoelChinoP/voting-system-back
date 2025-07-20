package com.auth.service;

import com.auth.dto.RegisterRequest;
import com.auth.model.User;
import com.auth.repository.UserRepository;

import java.time.LocalDateTime;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Constructor único: Spring lo inyecta automáticamente, no necesita @Autowired
    public AuthService(UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * RF-001: Registro de nuevos usuarios.
     * Lanza DataIntegrityViolationException si ya existe username.
     */
    public User register(RegisterRequest request) {
        // Verificar si el usuario ya existe
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new DataIntegrityViolationException("El email ya está registrado");
        }

        // Crear nuevo usuario
        User user = User.builder()
                .username(request.getUsername())
                .fullName(request.getFullName())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles("USER")
                .createdAt(LocalDateTime.now())
                .build();

        // Guardar y devolver el usuario
        return userRepository.save(user);
    }
}
