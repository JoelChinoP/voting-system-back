package com.auth.controller;

import com.auth.dto.LoginRequest;
import com.auth.dto.MarkVotedRequest;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.AuthenticationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.auth.service.RefreshTokenService;
import com.auth.service.TokenBlacklistService;
import com.auth.model.RefreshToken;
import com.auth.dto.RefreshTokenRequest;
import com.auth.dto.ValidateTokenRequest;
import com.auth.dto.PayloadRequest;
import com.auth.dto.LogoutRequest;
import com.auth.exception.TokenException;
import jakarta.servlet.http.HttpServletRequest;

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
        private final RefreshTokenService refreshTokenService;
        private final TokenBlacklistService tokenBlacklistService;

        @Value("${jwt.secret}")
        private String jwtSecret;

        @Value("${app.default-election-id:550e8400-e29b-41d4-a716-446655440000}")
        private String defaultElectionId;

        @Value("${jwt.refresh-expiration:604800000}")
        private Long refreshTokenDurationMs;

        private static final String HAS_VOTED = "hasVoted";

        /**
         * RF-001: Registro de usuarios
         */
        @PostMapping("/register")
        @Operation(summary = "Register a new user", description = "Register a new user with email and password validation")
        @ApiResponse(responseCode = "201", description = "User registered successfully")
        @ApiResponse(responseCode = "400", description = "Invalid request data")
        @ApiResponse(responseCode = "409", description = "Email already exists")
        public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest req) {
                // CAMBIO: Usar email en lugar de username en el log
                log.info("Registering new user: {}", req.getEmail());

                User newUser = authService.register(req);

                // Verificar estado de votación (nuevo usuario = false)
                boolean hasVoted = false;

                // Generar token con información completa
                String token = jwtUtil.generateTokenWithPayload(newUser, hasVoted);

                // Crear payload para la respuesta
                Map<String, Object> payload = Map.of(
                                "userId", newUser.getId().toString(),
                                "email", newUser.getUsername(),
                                "role", newUser.getRoles() != null ? newUser.getRoles() : "USER",
                                "hasVoted", hasVoted);

                // CAMBIO: Usar email en el log de éxito
                log.info("User registered successfully: {}", req.getEmail());
                return ResponseEntity.status(201).body(Map.of(
                                "message", "Usuario registrado exitosamente",
                                "user", Map.of(
                                                "id", newUser.getId(),
                                                "email", newUser.getUsername(),
                                                "fullName", newUser.getFullName(),
                                                "roles", newUser.getRoles() != null ? newUser.getRoles() : "USER",
                                                "isActive", newUser.isActive(),
                                                "createdAt",
                                                newUser.getCreatedAt() != null ? newUser.getCreatedAt()
                                                                : System.currentTimeMillis()),
                                "token", token,
                                "secret", jwtSecret,
                                "payload", payload,
                                "expiresIn", 86400000L,
                                "tokenType", "Bearer"));
        }

        /**
         * RF-002: Autenticación de usuarios
         */
        @PostMapping("/login")
        @Operation(summary = "Authenticate user", description = "Login with email and password to get JWT token and refresh token")
        @ApiResponse(responseCode = "200", description = "Authentication successful")
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
        public ResponseEntity<Map<String, Object>> login(
                        @Valid @RequestBody LoginRequest request,
                        HttpServletRequest httpRequest) {

                log.info("Login attempt for user: {} | ip: {}", request.getEmail(), getClientIp(httpRequest));

                try {
                        Authentication auth = authenticationManager.authenticate(
                                        new UsernamePasswordAuthenticationToken(request.getEmail(),
                                                        request.getPassword()));

                        // 1) Coger el UserDetails que construyó Spring
                        UserDetails userDetails = (UserDetails) auth.getPrincipal();
                        String email = userDetails.getUsername();

                        // 2) Recargar la entidad User completa desde la BD
                        User user = userRepository.findByEmail(email)
                                        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

                        // 3) Continuar igual que antes
                        boolean hasVoted = votingStatusService.hasUserVoted(
                                        user.getId(), UUID.fromString(defaultElectionId));
                        String token = jwtUtil.generateTokenWithPayload(user, hasVoted);

                        RefreshToken refreshToken = refreshTokenService.createRefreshToken(
                                        user, extractDeviceInfo(httpRequest), getClientIp(httpRequest));

                        List<String> roles = new ArrayList<>(user.getRoles());
                        Map<String, Object> payload = Map.of(
                                        "userId", user.getId().toString(),
                                        "email", user.getUsername(),
                                        "roles", roles,
                                        "hasVoted", hasVoted);

                        log.info("Login successful for user: {}", email);
                        return ResponseEntity.ok(Map.of(
                                        "token", token,
                                        "refreshToken", refreshToken.getToken(),
                                        "tokenType", "Bearer",
                                        "expiresIn", 86400,
                                        "refreshExpiresIn", refreshTokenDurationMs / 1000,
                                        "payload", payload));

                } catch (AuthenticationException ex) {
                        log.warn("Login failed for user {}: {}", request.getEmail(), ex.getMessage());
                        return ResponseEntity.status(401).body(Map.of(
                                        "error", "Credenciales inválidas",
                                        "message", ex.getMessage()));
                }
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
                                .orElseThrow(() -> new UsernameNotFoundException(
                                                "User not found: " + user.getUsername()));

                // Usar electionId por defecto si no se proporciona
                UUID targetElectionId = electionId != null ? electionId : UUID.fromString(defaultElectionId);

                // Verificar en Cassandra si ya votó
                boolean hasVotedInCassandra = votingStatusService.hasUserVoted(dbUser.getId(), targetElectionId);

                log.info("Voting status check for user {}: hasVoted={}",
                                dbUser.getUsername(), hasVotedInCassandra);

                return ResponseEntity.ok(Map.of(
                                HAS_VOTED, hasVotedInCassandra,
                                "userId", dbUser.getId().toString(),
                                "email", dbUser.getUsername(),
                                "role", dbUser.getRoles() != null ? dbUser.getRoles() : "USER",
                                "electionId", targetElectionId.toString(),
                                "eligible", !hasVotedInCassandra,
                                "message",
                                hasVotedInCassandra ? "User has already voted" : "User is eligible to vote"));
        }

        /**
         * POST /refresh - Renovación de access token usando refresh token
         */
        @PostMapping("/refresh")
        @Operation(summary = "Refresh access token", description = "Generate new access token using refresh token")
        @ApiResponse(responseCode = "200", description = "Token refreshed successfully")
        @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
        public ResponseEntity<Map<String, Object>> refreshToken(
                        @Valid @RequestBody RefreshTokenRequest request,
                        HttpServletRequest httpRequest) {

                String clientIp = getClientIp(httpRequest);
                log.info("Token refresh attempt | ip: {}", clientIp);

                try {
                        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(request.getRefreshToken());
                        User user = refreshToken.getUser();

                        boolean hasVoted = votingStatusService.hasUserVoted(user.getId(),
                                        UUID.fromString(defaultElectionId));
                        String newAccessToken = jwtUtil.generateTokenWithPayload(user, hasVoted);

                        String deviceInfo = extractDeviceInfo(httpRequest);
                        RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(refreshToken, deviceInfo,
                                        clientIp);

                        log.info("Token refreshed successfully | user: {} | oldTokenId: {} | newTokenId: {} | hasVoted: {} | ip: {}",
                                        user.getUsername(), refreshToken.getId(), newRefreshToken.getId(), hasVoted,
                                        clientIp);

                        // RESPUESTA DIRECTA SIN DTO INNECESARIO
                        return ResponseEntity.ok(Map.of(
                                        "accessToken", newAccessToken,
                                        "refreshToken", newRefreshToken.getToken(),
                                        "tokenType", "Bearer",
                                        "expiresIn", 86400L,
                                        "userId", user.getId().toString(),
                                        "email", user.getUsername(),
                                        "role", user.getRoles() != null ? user.getRoles() : "USER",
                                        "hasVoted", hasVoted,
                                        "timestamp", System.currentTimeMillis()));

                } catch (TokenException e) {
                        log.warn("Token refresh failed | error: {} | ip: {}", e.getMessage(), clientIp);
                        return ResponseEntity.status(401).body(Map.of(
                                        "error", "Invalid refresh token",
                                        "message", e.getMessage(),
                                        "timestamp", System.currentTimeMillis()));
                } catch (Exception e) {
                        log.error("Token refresh error | error: {} | ip: {}", e.getMessage(), clientIp, e);
                        return ResponseEntity.status(500).body(Map.of(
                                        "error", "Token refresh failed",
                                        "message", "Internal server error",
                                        "timestamp", System.currentTimeMillis()));
                }
        }

        /**
         * POST /validate - Validación rápida de token
         */
        @PostMapping("/validate")
        @Operation(summary = "Validate token", description = "Quick validation of JWT token")
        @ApiResponse(responseCode = "200", description = "Token validation result")
        public ResponseEntity<Map<String, Object>> validateToken(@Valid @RequestBody ValidateTokenRequest request) {
                try {
                        String token = request.getToken();

                        // USAR SERVICIO EN LUGAR DE MÉTODO LOCAL
                        String jti = jwtUtil.extractJti(token);
                        if (jti != null && tokenBlacklistService.isTokenRevoked(jti)) {
                                return ResponseEntity.ok(Map.of("valid", false, "reason", "Token is blacklisted"));
                        }

                        boolean isValid = jwtUtil.validateToken(token);
                        return ResponseEntity.ok(Map.of(
                                        "valid", isValid,
                                        "timestamp", System.currentTimeMillis()));

                } catch (Exception e) {
                        return ResponseEntity.ok(Map.of(
                                        "valid", false,
                                        "reason", e.getMessage()));
                }
        }

        /**
         * POST /payload - Introspección de token
         */
        @PostMapping("/payload")
        @Operation(summary = "Extract token payload", description = "Extract claims from JWT token")
        @ApiResponse(responseCode = "200", description = "Token payload extracted")
        @ApiResponse(responseCode = "401", description = "Invalid token")
        public ResponseEntity<Map<String, Object>> extractPayload(@Valid @RequestBody PayloadRequest request) {
                try {
                        String token = request.getToken();

                        // USAR SERVICIO EN LUGAR DE MÉTODO LOCAL
                        String jti = jwtUtil.extractJti(token);
                        if (jti != null && tokenBlacklistService.isTokenRevoked(jti)) {
                                return ResponseEntity.status(401).body(Map.of("error", "Token is blacklisted"));
                        }

                        Map<String, Object> payload = jwtUtil.extractPayload(token);
                        return ResponseEntity.ok(payload);

                } catch (Exception e) {
                        log.error("Error extracting payload: {}", e.getMessage());
                        return ResponseEntity.status(401).body(Map.of(
                                        "error", "Invalid token",
                                        "message", e.getMessage()));
                }
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
                                "jwtConfig", Map.of(
                                                "secretConfigured", jwtSecret != null && !jwtSecret.isEmpty(),
                                                "defaultElectionId", defaultElectionId),
                                "endpoints", Map.of(
                                                "register", "POST /api/v1/auth/register",
                                                "login", "POST /api/v1/auth/login",
                                                "votingStatus", "GET /api/v1/auth/voting-status (requires JWT)",
                                                "logout", "POST /api/v1/auth/logout (requires JWT)",
                                                "status", "GET /api/v1/auth/status")));
        }

        /**
         * RF-004: Cierre de sesión de usuarios (MODIFICADO)
         */
        @PostMapping("/logout")
        @Operation(summary = "Logout user", description = "Revoke refresh token and blacklist access token")
        @ApiResponse(responseCode = "200", description = "Logout successful")
        @ApiResponse(responseCode = "401", description = "Unauthorized")
        @SecurityRequirement(name = "bearerAuth")
        public ResponseEntity<Map<String, Object>> logout(
                        HttpServletRequest request,
                        @Valid @RequestBody LogoutRequest logoutRequest) {

                try {
                        String accessToken = extractTokenFromRequest(request);

                        if (logoutRequest.getRefreshToken() != null) {
                                refreshTokenService.revokeRefreshToken(logoutRequest.getRefreshToken());
                        }

                        if (accessToken != null) {
                                blacklistToken(accessToken);
                        }

                        log.info("User logged out successfully | timestamp={}", System.currentTimeMillis());

                        return ResponseEntity.ok(Map.of(
                                        "message", "Logged out successfully",
                                        "timestamp", System.currentTimeMillis()));

                } catch (Exception e) {
                        log.error("Error during logout | error={}", e.getMessage(), e);
                        return ResponseEntity.status(500).body(Map.of(
                                        "error", "Logout failed",
                                        "message", e.getMessage()));
                }
        }

        /**
         * RF-005: Marcar usuario como votado
         */
        @PostMapping("/mark-voted")
        @Operation(summary = "Mark user as voted", description = "Mark authenticated user as having voted and return updated token")
        @ApiResponse(responseCode = "200", description = "User marked as voted successfully")
        @ApiResponse(responseCode = "401", description = "Unauthorized")
        @ApiResponse(responseCode = "409", description = "User has already voted")
        @SecurityRequirement(name = "bearerAuth")
        public ResponseEntity<Map<String, Object>> markUserAsVoted(
                        @AuthenticationPrincipal UserDetails user,
                        @Valid @RequestBody MarkVotedRequest request) {

                User dbUser = userRepository.findByUsername(user.getUsername())
                                .orElseThrow(() -> new UsernameNotFoundException(
                                                "User not found: " + user.getUsername()));

                UUID electionId = request.getElectionId() != null ? request.getElectionId()
                                : UUID.fromString(defaultElectionId);

                // Verificar si ya votó
                boolean hasAlreadyVoted = votingStatusService.hasUserVoted(dbUser.getId(), electionId);
                if (hasAlreadyVoted) {
                        return ResponseEntity.status(409).body(Map.of(
                                        "error", "User has already voted",
                                        "hasVoted", true,
                                        "electionId", electionId.toString()));
                }

                try {
                        // Marcar como votado en Cassandra (esto lo haría el voting-service normalmente)
                        // Aquí solo actualizamos el estado para generar nuevo token

                        // Generar nuevo token con hasVoted=true
                        String newToken = jwtUtil.generateTokenWithPayload(dbUser, true);

                        Map<String, Object> response = Map.of(
                                        "message", "User marked as voted successfully",
                                        "hasVoted", true,
                                        "userId", dbUser.getId().toString(),
                                        "electionId", electionId.toString(),
                                        "newToken", newToken,
                                        "tokenType", "Bearer",
                                        "expiresIn", 86400);

                        log.info("User {} marked as voted for election {}", dbUser.getUsername(), electionId);
                        return ResponseEntity.ok(response);

                } catch (Exception e) {
                        log.error("Error marking user as voted: {}", e.getMessage(), e);
                        return ResponseEntity.status(500).body(Map.of(
                                        "error", "Failed to mark user as voted",
                                        "message", e.getMessage()));
                }
        }

        // Métodos auxiliares

        private void blacklistToken(String token) {
                try {
                        String jti = jwtUtil.extractJti(token);
                        if (jti != null) {
                                long expirationTime = jwtUtil.getExpirationTime(token);
                                long currentTime = System.currentTimeMillis();
                                long ttlSeconds = Math.max(0, (expirationTime - currentTime) / 1000);

                                if (ttlSeconds > 0) {
                                        tokenBlacklistService.addToBlacklist(jti, ttlSeconds);
                                        log.info("Token blacklisted successfully: JTI={}", jti);
                                }
                        }
                } catch (Exception e) {
                        log.error("Error blacklisting token: {}", e.getMessage());
                }
        }

        private String extractTokenFromRequest(HttpServletRequest request) {
                String bearerToken = request.getHeader("Authorization");
                if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                        return bearerToken.substring(7);
                }
                return null;
        }

        private String getClientIp(HttpServletRequest request) {
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                        return xForwardedFor.split(",")[0].trim();
                }

                String xRealIp = request.getHeader("X-Real-IP");
                if (xRealIp != null && !xRealIp.isEmpty()) {
                        return xRealIp;
                }

                return request.getRemoteAddr();
        }

        private String extractDeviceInfo(HttpServletRequest request) {
                String userAgent = request.getHeader("User-Agent");
                return userAgent != null ? userAgent.substring(0, Math.min(userAgent.length(), 500)) : "Unknown";
        }
}