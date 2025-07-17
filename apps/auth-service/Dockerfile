FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="Tu Nombre/Equipo <tu_email@ejemplo.com>"
LABEL description="Microservicio de Autenticación (Auth Service)"
LABEL version="1.0.0"

WORKDIR /app

COPY target/auth-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081

CMD ["java", "-jar", "app.jar"]

