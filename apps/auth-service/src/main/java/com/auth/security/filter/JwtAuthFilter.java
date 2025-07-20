package com.auth.security.filter;

import com.auth.security.JwtUtil;
import com.auth.security.UserDetailsServiceImpl;
import com.auth.service.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    // Constructor injection
    public JwtAuthFilter(JwtUtil jwtUtil, UserDetailsServiceImpl userDetailsService,
            TokenBlacklistService tokenBlacklistService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    protected void doFilterInternal(@org.springframework.lang.NonNull HttpServletRequest request,
            @org.springframework.lang.NonNull HttpServletResponse response,
            @org.springframework.lang.NonNull FilterChain filterChain)
            throws ServletException, IOException {
        if (request == null || response == null || filterChain == null) {
            throw new IllegalArgumentException("Request, Response, or FilterChain cannot be null");
        }

        final String authHeader = request.getHeader("Authorization");
        String username = null;
        String token = null;

        log.debug("Processing request: {} {}", request.getMethod(), request.getRequestURI());

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            log.debug("Token found: {}", token.substring(0, Math.min(token.length(), 20)) + "...");

            try {
                username = jwtUtil.extractUsername(token);
                log.debug("Username extracted: {}", username);
            } catch (Exception e) {
                log.error("Error extracting username from token: {}", e.getMessage());
            }
        } else {
            log.debug("No Authorization header or doesn't start with Bearer");
        }

        // Verificar blacklist ANTES de validar el token
        if (token != null) {
            try {
                String jti = jwtUtil.extractJti(token);
                if (jti != null && tokenBlacklistService.isTokenRevoked(jti)) {
                    log.warn("Token is revoked (JTI: {}): {}", jti,
                            token.substring(0, Math.min(token.length(), 20)) + "...");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"error\":\"Token is revoked\"}");
                    response.setContentType("application/json");
                    return;
                }
            } catch (Exception e) {
                log.error("Error checking token blacklist: {}", e.getMessage());
                // Continuar con la validación normal del token
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            log.debug("Username found and no existing authentication, validating token");
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtUtil.validateToken(token)) {
                log.debug("Token is valid, setting authentication for user: {}", username);
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
                        null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                log.warn("Token validation failed for user: {}", username);
            }
        }

        filterChain.doFilter(request, response);
    }
}
