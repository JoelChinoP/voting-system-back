FROM openjdk:17-jdk-slim

WORKDIR /app

# Copiar el JAR específico por nombre
COPY target/reports-service-0.0.1-SNAPSHOT.jar app.jar

# Verificar que el JAR existe
RUN ls -la app.jar

# Exponer el puerto
EXPOSE 8084

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]