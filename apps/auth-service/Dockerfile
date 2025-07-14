# Etapa 1: Compilación
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Copiar archivos necesarios para construir el JAR
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
COPY src ./src

# Compilar usando Maven wrapper
RUN ./mvnw clean package -DskipTests

# Etapa 2: Imagen final para ejecución
FROM eclipse-temurin:17-jdk
WORKDIR /app

# Copiar el JAR desde la etapa de construcción
COPY --from=build /app/target/auth-0.0.1-SNAPSHOT.jar app.jar

# Puerto expuesto (opcional)
EXPOSE 8080

# Comando de arranque
ENTRYPOINT ["java", "-jar", "app.jar"]
