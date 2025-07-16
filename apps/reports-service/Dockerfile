# Usar una imagen base de OpenJDK 17
FROM openjdk:17-jdk-slim

# Información del mantenedor
LABEL maintainer="Sistema de Votación Electrónica <desarrollo@votacion.com>"
LABEL description="Microservicio de reportes para el sistema de votación electrónica"
LABEL version="1.0.0"

# Crear un usuario no-root para ejecutar la aplicación
RUN groupadd -r reports && useradd -r -g reports reports

# Establecer el directorio de trabajo
WORKDIR /app

# Copiar el archivo JAR de la aplicación
COPY target/reports-service-*.jar app.jar

# Cambiar la propiedad del archivo a nuestro usuario
RUN chown reports:reports app.jar

# Cambiar al usuario no-root
USER reports

# Exponer el puerto en el que la aplicación se ejecuta
EXPOSE 8084

# Variables de entorno por defecto
ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV SPRING_PROFILES_ACTIVE="prod"

# Health check para verificar que la aplicación esté funcionando
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8084/actuator/health || exit 1

# Comando para ejecutar la aplicación
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

