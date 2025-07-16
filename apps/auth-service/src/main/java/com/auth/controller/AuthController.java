package com.auth.controller;

import com.auth.dto.LoginRequest;
import com.auth.dto.LoginResponse;
import com.auth.dto.RegisterRequest;
import com.auth.model.User;
import com.auth.repository.UserRepository;
import com.auth.security.JwtUtil;
import com.auth.service.AuthService;
import com.auth.service.VotingStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@CrossOrigin(
        origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:4173"},
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS},
        allowCredentials = "true"
)
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "User authentication and authorization endpoints")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final VotingStatusService votingStatusService;

    /**
     * RF-001: Registro de usuarios
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Register a new user with email and password validation")
    @ApiResponse(responseCode = "201", description = "User registered successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "409", description = "Email already exists")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest req) {
        log.info("Registering new user: {}", req.getUsername());
        
        authService.register(req);
        
        log.info("User registered successfully: {}", req.getUsername());
        return ResponseEntity.status(201).body(Map.of(
            "message", "Usuario registrado exitosamente",
            "email", req.getUsername()
        ));
    }

    /**
     * RF-002: Autenticación de usuarios
     */
    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = "Login with email and password to get JWT token")
    @ApiResponse(responseCode = "200", description = "Authentication successful")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());
        
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String token = jwtUtil.generateToken(userDetails);
        
        log.info("Login successful for user: {}", request.getUsername());
        return ResponseEntity.ok(new LoginResponse(token));
    }

    /**
     * RF-003: Verificación de elegibilidad para votar
     */
    @GetMapping("/voting-status")
    @Operation(summary = "Check voting eligibility", description = "Check if the authenticated user has already voted")
    @ApiResponse(responseCode = "200", description = "Voting status retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, Object>> checkVotingStatus(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam(value = "electionId", required = false) UUID electionId) {
        
        User dbUser = userRepository.findByUsername(user.getUsername())
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + user.getUsername()));
        
        // Verificar en Cassandra si ya votó
        boolean hasVotedInCassandra = votingStatusService.hasUserVoted(dbUser.getId(), electionId);
        
        log.info("Voting status check for user {}: hasVoted={}", 
            dbUser.getUsername(), hasVotedInCassandra);
        
        return ResponseEntity.ok(Map.of(
            "hasVoted", hasVotedInCassandra,
            "userId", dbUser.getId(),
            "eligible", !hasVotedInCassandra,
            "message", hasVotedInCassandra ? "User has already voted" : "User is eligible to vote"
        ));
    }

    /**
     * Endpoint legacy para compatibilidad (deprecated)
     */
    @GetMapping("/has-voted")
    @Operation(summary = "Check if user has voted (legacy)", description = "Legacy endpoint - use /voting-status instead")
    @ApiResponse(responseCode = "200", description = "Voting status retrieved")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @SecurityRequirement(name = "bearerAuth")
    @Deprecated
    public ResponseEntity<Map<String, Boolean>> hasVoted(@AuthenticationPrincipal UserDetails user) {
        User dbUser = userRepository.findByUsername(user.getUsername())
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + user.getUsername()));
        
        boolean hasVoted = votingStatusService.hasUserVoted(dbUser.getId());
        
        return ResponseEntity.ok(Map.of("hasVoted", hasVoted));
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/status")
    @Operation(summary = "Service status", description = "Check if the auth service is running")
    @ApiResponse(responseCode = "200", description = "Service is running")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(Map.of(
            "service", "auth-service",
            "status", "RUNNING",
            "version", "1.0.0",
            "port", 8081,
            "timestamp", System.currentTimeMillis(),
            "endpoints", Map.of(
                "register", "POST /api/v1/auth/register",
                "login", "POST /api/v1/auth/login", 
                "votingStatus", "GET /api/v1/auth/voting-status (requires JWT)",
                "hasVoted", "GET /api/v1/auth/has-voted (requires JWT, deprecated)",
                "status", "GET /api/v1/auth/status"
            )
        ));
    }
}
