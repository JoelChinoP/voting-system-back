FROM openjdk:17-jdk-slim

LABEL maintainer="Tu Nombre/Equipo <tu_email@ejemplo.com>"
LABEL description="Microservicio de Usuarios (Users Service)"
LABEL version="1.0.0"

RUN groupadd -r appusers && useradd -r -g appusers appusers

WORKDIR /app

COPY target/users-0.0.1-SNAPSHOT.jar app.jar

RUN chown appusers:appusers app.jar

USER appusers

EXPOSE 8082

ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV SPRING_PROFILES_ACTIVE="prod"

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8082/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
