package com.auth.service;

import com.auth.dto.RegisterRequest;
import com.auth.model.User;
import com.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setFullName("Juan Pérez");
        registerRequest.setEmail("juan@example.com");
        registerRequest.setPassword("Password123");
    }

    @Test
    void testRegister_Success() {
        // Given
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(new User());

        // When
        assertDoesNotThrow(() -> authService.register(registerRequest));

        // Then
        verify(userRepository).findByUsername("juan@example.com");
        verify(passwordEncoder).encode("Password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegister_EmailAlreadyExists() {
        // Given
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(new User()));

        // When & Then
        assertThrows(DataIntegrityViolationException.class, () -> {
            authService.register(registerRequest);
        });

        verify(userRepository).findByUsername("juan@example.com");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }
}
