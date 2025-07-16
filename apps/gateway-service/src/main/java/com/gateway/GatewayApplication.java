package com.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Auth Service - Puerto 8081
                .route("auth-service", r -> r.path("/api/auth/**")
                        .uri("http://localhost:8081/api/v1/auth"))
                
                // Users Service - Puerto 8082
                .route("users-service", r -> r.path("/api/candidates/**")
                        .uri("http://localhost:8082/api/v1/candidates"))
                
                // Votes Service - Puerto 8083
                .route("votes-service", r -> r.path("/api/votes/**")
                        .uri("http://localhost:8083/api/v1/votes"))
                
                // Reports Service - Puerto 8084
                .route("reports-service", r -> r.path("/api/reports/**")
                        .uri("http://localhost:8084/api/v1/reports"))
                
                // Health checks para cada servicio
                .route("auth-health", r -> r.path("/health/auth")
                        .filters(f -> f.rewritePath("/health/auth", "/actuator/health"))
                        .uri("http://localhost:8081"))
                
                .route("users-health", r -> r.path("/health/users")
                        .filters(f -> f.rewritePath("/health/users", "/actuator/health"))
                        .uri("http://localhost:8082"))
                
                .route("votes-health", r -> r.path("/health/votes")
                        .filters(f -> f.rewritePath("/health/votes", "/actuator/health"))
                        .uri("http://localhost:8083"))
                
                .route("reports-health", r -> r.path("/health/reports")
                        .filters(f -> f.rewritePath("/health/reports", "/actuator/health"))
                        .uri("http://localhost:8084"))
                
                // Swagger UI para cada servicio
                .route("auth-swagger", r -> r.path("/swagger/auth/**")
                        .filters(f -> f.rewritePath("/swagger/auth/(?<segment>.*)", "/${segment}"))
                        .uri("http://localhost:8081"))
                
                .route("users-swagger", r -> r.path("/swagger/users/**")
                        .filters(f -> f.rewritePath("/swagger/users/(?<segment>.*)", "/${segment}"))
                        .uri("http://localhost:8082"))
                
                .route("votes-swagger", r -> r.path("/swagger/votes/**")
                        .filters(f -> f.rewritePath("/swagger/votes/(?<segment>.*)", "/${segment}"))
                        .uri("http://localhost:8083"))
                
                .route("reports-swagger", r -> r.path("/swagger/reports/**")
                        .filters(f -> f.rewritePath("/swagger/reports/(?<segment>.*)", "/${segment}"))
                        .uri("http://localhost:8084"))
                
                .build();
    }
}