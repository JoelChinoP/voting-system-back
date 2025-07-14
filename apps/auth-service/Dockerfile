# Etapa final: imagen ligera con Java 17
FROM eclipse-temurin:17-jdk

# Carpeta de trabajo dentro del contenedor
WORKDIR /app

# Copiar el .jar generado por Maven local
COPY target/auth-0.0.1-SNAPSHOT.jar app.jar

# Exponer el puerto por defecto de Spring Boot
EXPOSE 8080

# Comando para ejecutar el servicio
ENTRYPOINT ["java", "-jar", "app.jar"]