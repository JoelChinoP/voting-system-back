FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="Tu Nombre/Equipo <tu_email@ejemplo.com>"
LABEL description="Microservicio de Usuarios (Users Service)"
LABEL version="1.0.0"

WORKDIR /app

COPY target/users-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8082

CMD ["java", "-jar", "app.jar"]
