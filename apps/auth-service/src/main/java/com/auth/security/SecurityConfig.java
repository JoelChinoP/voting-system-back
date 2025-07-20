package com.auth.security;

import com.auth.security.filter.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.beans.factory.annotation.Value;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;

    @Value("${security.cors.allowed-origins:http://localhost:3000,http://localhost:8080}")
    private List<String> allowedOrigins;

    @Value("${security.cors.allow-credentials:true}")
    private boolean allowCredentials;

    @Value("${security.cors.max-age:3600}")
    private long maxAge;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            // 1) CSRF off
            .csrf(csrf -> csrf.disable())

            // 2) CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // 3) Stateless session
            .sessionManagement(sm -> sm
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // 4) Configurar filtros sólo para /api/v1/**
            .securityMatcher("/api/v1/**")

            // 5) Autenticación/autorización
            .authorizeHttpRequests(auth -> auth
                // públicos
                .requestMatchers(
                    "/api/v1/auth/register",
                    "/api/v1/auth/login",
                    "/api/v1/auth/status",
                    "/api/v1/auth/refresh",
                    "/api/v1/auth/validate",
                    "/api/v1/auth/payload",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/actuator/health",
                    "/auth/**"
                ).permitAll()
                // protegidos
                .anyRequest().authenticated()
            )

            // 6) Proveedor de autenticación
            .authenticationProvider(authenticationProvider())

            // 7) JWT filter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

            // 8) Build
            .build();
    }

    /**
     * AGREGADO: Proveedor de autenticación DAO
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt factor 12 para mayor seguridad
        return new BCryptPasswordEncoder(12);
    }

    /**
     * CORREGIDO: Configuración CORS segura y funcional
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // CORREGIDO: Orígenes específicos en lugar de "*"
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:3000", // React development
                "http://localhost:8080", // Vue.js development
                "http://localhost:4200", // Angular development
                "http://127.0.0.1:*", // Local testing
                "https://*.voting-app.com" // Production domain
        ));

        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Headers permitidos
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Requested-With",
                "Cache-Control",
                "Access-Control-Allow-Headers",
                "Access-Control-Allow-Origin"));

        // Headers expuestos al cliente
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type"));

        // CORREGIDO: Permitir credenciales solo con orígenes específicos
        configuration.setAllowCredentials(true);

        // Cache para preflight requests (1 hora)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}