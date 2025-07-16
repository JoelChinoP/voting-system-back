FROM openjdk:17-jdk-slim

LABEL maintainer="Tu Nombre/Equipo <tu_email@ejemplo.com>"
LABEL description="Microservicio de Autenticación (Auth Service)"
LABEL version="1.0.0"

RUN groupadd -r auth && useradd -r -g auth auth

WORKDIR /app

COPY target/auth-0.0.1-SNAPSHOT.jar app.jar

RUN chown auth:auth app.jar

USER auth

EXPOSE 8081

ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV SPRING_PROFILES_ACTIVE="prod"

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

