# Servicio de Autenticación

## Descripción General

El Servicio de Autenticación es un microservicio Spring Boot responsable de la autenticación y autorización de usuarios dentro del sistema de votación distribuido. Proporciona registro seguro de usuarios, autenticación basada en JWT, y verificación de elegibilidad para votar a través de la integración con bases de datos PostgreSQL y Cassandra.

## Tabla de Contenidos

- [Características](#características)
- [Arquitectura](#arquitectura)
- [Stack Tecnológico](#stack-tecnológico)
- [Documentación de API](#documentación-de-api)
- [Instalación](#instalación)
- [Configuración](#configuración)
- [Uso](#uso)
- [Pruebas](#pruebas)
- [Seguridad](#seguridad)
- [Monitoreo](#monitoreo)
- [Contribución](#contribución)

## Características

### Funcionalidades Principales

El servicio implementa los siguientes requerimientos funcionales:

#### RF-001: Registro de Usuarios
- Validación exhaustiva de datos de entrada del usuario
- Verificación de email único
- Aplicación de fuerza de contraseña (mínimo 8 caracteres, mayúscula, minúscula, numérico)
- Hash de contraseña BCrypt con factor de costo 12
- Persistencia de datos en PostgreSQL

#### RF-002: Autenticación de Usuarios
- Inicio de sesión basado en email/contraseña
- Generación de token JWT usando algoritmo HS256
- Expiración de token de 24 horas
- Gestión de sesiones sin estado

#### RF-003: Verificación de Elegibilidad para Votar
- Integración con Cassandra para consultas de estado de voto
- Verificación de elegibilidad en tiempo real
- Soporte para múltiples escenarios de elección
- Acceso a endpoints protegidos con validación JWT

## Arquitectura

### Componentes del Sistema

```
┌─────────────────────┐    ┌─────────────────────┐
│                     │    │                     │
│   Controlador de    │    │    PostgreSQL      │
│   Autenticación     │◄──►│ (Almacén Usuario)  │
│                     │    │                     │
└─────────────────────┘    └─────────────────────┘
           │
           ▼
┌─────────────────────┐    ┌─────────────────────┐
│                     │    │                     │
│   Servicio de       │    │     Cassandra      │
│   Autenticación     │◄──►│ (Seguimiento Voto) │
│                     │    │                     │
└─────────────────────┘    └─────────────────────┘
```

### Flujo de Datos

1. **Registro**: Usuario → Controlador → Servicio → PostgreSQL
2. **Autenticación**: Usuario → Controlador → AuthenticationManager → Generación JWT
3. **Estado de Votación**: JWT → Controlador → Servicio → Cassandra → Respuesta

## Stack Tecnológico

### Tecnologías Principales
- **Java 17**: Lenguaje de programación principal
- **Spring Boot 3.x**: Framework de aplicación
- **Spring Security**: Framework de seguridad
- **Spring Data JPA**: Integración con PostgreSQL
- **Spring Data Cassandra**: Integración con Cassandra
- **Maven**: Automatización de construcción y gestión de dependencias

### Tecnologías de Seguridad
- **JWT (JSON Web Tokens)**: Tokens de autenticación
- **BCrypt**: Hash de contraseñas
- **Biblioteca JJWT**: Implementación JWT

### Tecnologías de Base de Datos
- **PostgreSQL**: Base de datos relacional para datos de usuario
- **Cassandra**: Base de datos NoSQL para seguimiento de votos

### Documentación y Pruebas
- **SpringDoc OpenAPI**: Documentación de API
- **JUnit 5**: Framework de pruebas unitarias
- **Mockito**: Framework de mocking para pruebas

## Documentación de API

### URL Base
```
http://localhost:8081/api/v1/auth
```

### Endpoints Públicos

#### Registro de Usuario
```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "username": "usuario@ejemplo.com",
  "fullName": "Juan Pérez",
  "password": "ClaveSegura123"
}
```

**Respuesta (201 Created):**
```json
{
  "message": "Usuario registrado exitosamente",
  "email": "usuario@ejemplo.com"
}
```

#### Autenticación de Usuario
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "usuario@ejemplo.com",
  "password": "ClaveSegura123"
}
```

**Respuesta (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### Estado del Servicio
```http
GET /api/v1/auth/status
```

**Respuesta (200 OK):**
```json
{
  "service": "auth-service",
  "status": "RUNNING",
  "version": "1.0.0",
  "port": 8081,
  "timestamp": 1626284400000,
  "endpoints": {
    "register": "POST /api/v1/auth/register",
    "login": "POST /api/v1/auth/login",
    "votingStatus": "GET /api/v1/auth/voting-status (requiere JWT)",
    "status": "GET /api/v1/auth/status"
  }
}
```

### Endpoints Protegidos

#### Verificación de Estado de Votación
```http
GET /api/v1/auth/voting-status?electionId=550e8400-e29b-41d4-a716-446655440000
Authorization: Bearer <jwt-token>
```

**Respuesta (200 OK):**
```json
{
  "hasVoted": false,
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "eligible": true,
  "message": "El usuario es elegible para votar"
}
```

### Respuestas de Error

#### 400 Bad Request
```json
{
  "error": "Falló la validación",
  "message": "La contraseña debe contener al menos una letra mayúscula",
  "timestamp": "2025-07-14T19:25:35.000+00:00"
}
```

#### 401 Unauthorized
```json
{
  "error": "Falló la autenticación",
  "message": "Credenciales inválidas",
  "timestamp": "2025-07-14T19:25:35.000+00:00"
}
```

#### 409 Conflict
```json
{
  "error": "Conflicto de recurso",
  "message": "El email ya está registrado",
  "timestamp": "2025-07-14T19:25:35.000+00:00"
}
```

### Documentación Interactiva

El servicio proporciona documentación interactiva de API a través de Swagger UI:
- **Swagger UI**: http://localhost:8081/swagger-ui.html
- **Especificación OpenAPI**: http://localhost:8081/v3/api-docs

## Instalación

### Prerrequisitos
- Java 17 o superior
- Maven 3.6+
- PostgreSQL 13+
- Cassandra 4.0+

### Instrucciones de Construcción

```bash
# Clonar el repositorio
git clone <url-del-repositorio>

# Navegar al directorio auth-service
cd voting-system-back/apps/auth-service

# Limpiar y compilar
./mvnw clean compile

# Ejecutar pruebas
./mvnw test

# Empaquetar aplicación
./mvnw package

# Ejecutar la aplicación
./mvnw spring-boot:run
```

### Despliegue con Docker

```bash
# Construir imagen Docker
docker build -t auth-service:latest .

# Ejecutar con Docker Compose
docker-compose up -d auth-service
```

## Configuración

### Configuración de Base de Datos

#### PostgreSQL (application.properties)
```properties
# Configuración PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/voting_db
spring.datasource.username=voting_user
spring.datasource.password=voting_password
spring.datasource.driver-class-name=org.postgresql.Driver

# Configuración JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.format_sql=true
```

#### Configuración de Cassandra
```properties
# Configuración Cassandra
spring.cassandra.contact-points=localhost
spring.cassandra.port=9042
spring.cassandra.keyspace-name=voting_keyspace
spring.cassandra.local-datacenter=datacenter1
spring.cassandra.username=cassandra
spring.cassandra.password=cassandra
```

### Configuración de Seguridad

#### Configuración JWT
```properties
# Configuración JWT
jwt.secret=tu-clave-secreta-256-bits-aqui
jwt.expiration=86400000
jwt.issuer=auth-service
```

#### Configuración BCrypt
```properties
# Codificación de Contraseñas
security.password.strength=12
```

### Configuración de Aplicación

#### Configuración del Servidor
```properties
# Configuración del Servidor
server.port=8081
server.servlet.context-path=/

# Configuración de la Aplicación
spring.application.name=auth-service
app.default-election-id=550e8400-e29b-41d4-a716-446655440000
```

#### Configuración de Logging
```properties
# Configuración de Logging
logging.level.com.auth=DEBUG
logging.level.org.springframework.security=DEBUG
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n
```

## Uso

### Entorno de Desarrollo

#### Iniciar Servicios de Base de Datos
```bash
# Iniciar PostgreSQL
docker run -d --name postgres-auth \
  -e POSTGRES_DB=voting_db \
  -e POSTGRES_USER=voting_user \
  -e POSTGRES_PASSWORD=voting_password \
  -p 5432:5432 postgres:13

# Iniciar Cassandra
docker run -d --name cassandra-auth \
  -p 9042:9042 cassandra:4.0
```

#### Inicializar Esquema de Base de Datos
```bash
# Inicialización PostgreSQL
psql -h localhost -U voting_user -d voting_db -f config/docker/scripts/db/postgres/01-init.sql

# Creación de keyspace Cassandra
cqlsh -f config/docker/scripts/db/cassandra/keyspace.cql
```

#### Ejecutar Aplicación
```bash
# Modo desarrollo
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Modo producción
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

### Entorno de Producción

#### Variables de Entorno
```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://prod-db:5432/voting_db
export SPRING_DATASOURCE_USERNAME=prod_user
export SPRING_DATASOURCE_PASSWORD=secure_password
export SPRING_CASSANDRA_CONTACT_POINTS=cassandra-cluster
export JWT_SECRET=production-secret-key-256-bits
export SERVER_PORT=8081
```

#### Verificaciones de Salud
```bash
# Salud de la aplicación
curl http://localhost:8081/api/v1/auth/status

# Conectividad de base de datos
curl http://localhost:8081/actuator/health
```

## Pruebas

### Pruebas Unitarias

#### Ejecutar Pruebas
```bash
# Ejecutar todas las pruebas
./mvnw test

# Ejecutar clase de prueba específica
./mvnw test -Dtest=AuthServiceTest

# Ejecutar pruebas con cobertura
./mvnw test jacoco:report
```

#### Estructura de Pruebas
```
src/test/java/com/auth/
├── service/
│   ├── AuthServiceTest.java
│   └── VotingStatusServiceTest.java
├── controller/
│   └── AuthControllerTest.java
├── security/
│   └── JwtUtilTest.java
└── integration/
    └── AuthIntegrationTest.java
```

### Pruebas de Integración

#### Integración de Base de Datos
```bash
# Ejecutar con base de datos de prueba
./mvnw test -Dspring.profiles.active=test

# Ejecutar con Testcontainers
./mvnw test -Dspring.profiles.active=testcontainers
```

#### Pruebas de API con Postman
```bash
# Importar colección
newman run docs/postman/auth-service-collection.json

# Ejecutar entorno específico
newman run docs/postman/auth-service-collection.json -e docs/postman/dev-environment.json
```

## Seguridad

### Flujo de Autenticación

1. **Registro de Usuario**: Las contraseñas se hashean usando BCrypt con sal
2. **Proceso de Login**: Credenciales validadas contra la base de datos
3. **Generación de Token**: JWT firmado con algoritmo HS256
4. **Validación de Token**: Cada petición valida la firma del token y expiración
5. **Autorización**: Endpoints protegidos requieren JWT válido en el header Authorization

### Headers de Seguridad

El servicio implementa los siguientes headers de seguridad:
- `X-Content-Type-Options: nosniff`
- `X-Frame-Options: DENY`
- `X-XSS-Protection: 1; mode=block`
- `Strict-Transport-Security: max-age=31536000; includeSubDomains`

### Configuración CORS

```java
@CrossOrigin(origins = {
    "http://localhost:3000",  // Desarrollo React
    "http://localhost:8080",  // Desarrollo Vue.js
    "https://voting-app.com"  // Frontend producción
})
```

### Validación de Entrada

#### Validación de Registro
- **Nombre Completo**: 2-100 caracteres, solo letras y espacios
- **Email**: Formato de email válido, restricción única
- **Contraseña**: Mínimo 8 caracteres, al menos una mayúscula, minúscula y carácter numérico

#### Validación JWT
- **Firma**: Validación HMAC-SHA256
- **Expiración**: Tiempo de vida del token de 24 horas
- **Emisor**: Verificado contra el emisor configurado
- **Sujeto**: Debe contener un identificador de usuario válido

## Monitoreo

### Verificaciones de Salud

El servicio proporciona monitoreo integral de salud:

#### Salud de la Aplicación
```bash
curl http://localhost:8081/api/v1/auth/status
```

#### Salud de Base de Datos
```bash
curl http://localhost:8081/actuator/health/db
```

### Logging

#### Niveles de Log
- **ERROR**: Errores del sistema y excepciones
- **WARN**: Violaciones de seguridad y actividad sospechosa
- **INFO**: Eventos de autenticación y lógica de negocio
- **DEBUG**: Información detallada de request/response

#### Formato de Log
```
2025-07-14 19:25:35 [http-nio-8081-exec-1] INFO  c.a.controller.AuthController - Intento de login para usuario: usuario@ejemplo.com
2025-07-14 19:25:35 [http-nio-8081-exec-1] INFO  c.a.controller.AuthController - Login exitoso para usuario: usuario@ejemplo.com
```

### Métricas

#### Métricas de Rendimiento
- **Tiempo de Respuesta**: Promedio 10-414ms
- **Throughput**: Pool de hilos configurable
- **Uso de Memoria**: Configuración JVM optimizada
- **Conexiones de Base de Datos**: Monitoreo de pool de conexiones

#### Métricas de Seguridad
- **Intentos de Login Fallidos**: Rastreados por usuario
- **Fallas de Validación de Token**: Monitoreadas por actividad sospechosa
- **Intentos de Registro**: Rastreados para limitación de tasa

## Contribución

### Directrices de Desarrollo

#### Estilo de Código
- Seguir la Guía de Estilo Java de Google
- Usar Lombok para reducir código boilerplate
- Implementar documentación JavaDoc comprehensiva
- Mantener cobertura de pruebas superior al 80%

#### Flujo de Trabajo Git
```bash
# Crear rama de característica
git checkout -b feat/nueva-caracteristica

# Confirmar cambios
git commit -m "feat(auth): implementar nueva característica de autenticación"

# Hacer push y crear PR
git push origin feat/nueva-caracteristica
```

#### Proceso de Pull Request
1. Crear rama de característica desde `main`
2. Implementar cambios con pruebas
3. Actualizar documentación
4. Enviar PR con descripción detallada
5. Revisión de código y aprobación
6. Merge a `main`

### Estructura del Proyecto

```
apps/auth-service/
├── src/
│   ├── main/
│   │   ├── java/com/auth/
│   │   │   ├── AuthServiceApplication.java
│   │   │   ├── config/
│   │   │   │   └── OpenApiConfig.java
│   │   │   ├── controller/
│   │   │   │   └── AuthController.java
│   │   │   ├── dto/
│   │   │   │   ├── LoginRequest.java
│   │   │   │   ├── LoginResponse.java
│   │   │   │   └── RegisterRequest.java
│   │   │   ├── entity/
│   │   │   │   └── cassandra/
│   │   │   │       └── UserVoteLog.java
│   │   │   ├── model/
│   │   │   │   └── User.java
│   │   │   ├── repository/
│   │   │   │   ├── UserRepository.java
│   │   │   │   └── cassandra/
│   │   │   │       └── UserVoteLogRepository.java
│   │   │   ├── security/
│   │   │   │   ├── JwtUtil.java
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   └── filter/
│   │   │   │       └── JwtAuthFilter.java
│   │   │   └── service/
│   │   │       ├── AuthService.java
│   │   │       ├── VotingStatusService.java
│   │   │       └── VotingStatusServiceTestImpl.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── application-test.properties
│   └── test/
│       └── java/com/auth/
│           └── service/
│               └── AuthServiceTest.java
├── target/
├── Dockerfile
├── pom.xml
└── README.md
```

---

**Versión del Servicio**: 1.0.0  
**Última Actualización**: 14 de Julio, 2025  
**Mantenedor**: Equipo de Desarrollo  
**Licencia**: MIT
