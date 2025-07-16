FROM openjdk:17-jdk-slim

LABEL maintainer="Tu Nombre/Equipo <tu_email@ejemplo.com>"
LABEL description="Microservicio de Usuarios (Users Service)"
LABEL version="1.0.0"

WORKDIR /app

COPY target/votes-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8083

CMD ["java", "-jar", "app.jar"]
