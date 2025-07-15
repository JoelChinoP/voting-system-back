package com.users.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Users Service - Gestión de Candidatos",
        description = "API REST para la gestión de candidatos electorales dentro del sistema de votación",
        version = "1.0.0",
        contact = @Contact(
            name = "Equipo de Desarrollo",
            email = "desarrollo@votingsystem.com"
        )
    ),
    servers = {
        @Server(url = "http://localhost:8081", description = "Servidor de Desarrollo"),
        @Server(url = "https://api.votingsystem.com", description = "Servidor de Producción")
    }
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "Token JWT para autenticación. Formato: 'Bearer {token}'"
)
public class OpenApiConfig {
}
