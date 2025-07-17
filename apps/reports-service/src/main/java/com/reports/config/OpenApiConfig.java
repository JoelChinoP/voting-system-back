package com.reports.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de OpenAPI (Swagger) para el microservicio de reportes.
 * 
 * Esta configuración define la documentación de la API REST del servicio,
 * incluyendo información general, esquemas de seguridad y metadatos.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI reportsServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Reports Service API")
                        .description("API del microservicio de reportes para el sistema de votación electrónica")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("Equipo de Desarrollo")
                                .email("desarrollo@votacion.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Token JWT para autenticación")));
    }
}

