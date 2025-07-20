# Build stage
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Actualizar sistema para seguridad
RUN apk update && apk upgrade

# Copiar el POM padre primero
COPY ../../pom.xml ./parent-pom.xml

# Copiar el POM del proyecto y las fuentes
COPY pom.xml .
COPY src src

# Modificar el POM para usar el parent local o usar standalone build
RUN mvn clean package -DskipTests -Dstandalone.build=true

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="Tu Nombre/Equipo <tu_email@ejemplo.com>"
LABEL description="Microservicio de Autenticación (Auth Service)"
LABEL version="1.0.0"

# Actualizar sistema e instalar curl
RUN apk update && apk upgrade && apk add --no-cache curl

# Crear usuario
RUN addgroup --system spring && adduser --system spring --ingroup spring

WORKDIR /app

# Copiar JAR desde build stage
COPY --from=build --chown=spring:spring /app/target/*.jar app.jar

# Variables de entorno
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8081

# Exponer puerto
EXPOSE 8081

# Cambiar a usuario spring
USER spring

# Comando de inicio
ENTRYPOINT ["java", "-jar", "app.jar"]