# Servicio de Autenticación

## Descripción General

El Servicio de Autenticación es un microservicio Spring Boot responsable de la autenticación y autorización de usuarios dentro del sistema de votación distribuido. Proporciona registro seguro de usuarios, autenticación basada en JWT con refresh tokens, gestión de blacklist de tokens, y verificación de elegibilidad para votar a través de la integración con bases de datos PostgreSQL, Cassandra y Redis.

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
- Generación de access token JWT usando algoritmo HS256 (24 horas)
- Generación de refresh token seguro (7 días)
- Rotación automática de refresh tokens
- Gestión de sesiones sin estado
- Tracking de dispositivos e IPs para auditoría

#### RF-003: Verificación de Elegibilidad para Votar
- Integración con Cassandra para consultas de estado de voto
- Verificación de elegibilidad en tiempo real
- Soporte para múltiples escenarios de elección
- Acceso a endpoints protegidos con validación JWT

#### RF-004: Gestión Avanzada de Tokens
- **POST /refresh**: Renovación de access tokens usando refresh tokens
- **POST /validate**: Validación rápida de tokens JWT
- **POST /payload**: Introspección de tokens (extracción de claims)
- **POST /logout**: Revocación de tokens y blacklist
- **POST /mark-voted**: Actualización de estado de voto con nuevo token

#### RF-005: Seguridad Avanzada
- Blacklist de tokens con Redis
- JTI (JWT ID) único para cada token
- Limpieza automática de tokens expirados
- Límite de refresh tokens por usuario (máximo 5)
- Detección de actividad sospechosa

## Arquitectura

### Componentes del Sistema

```
┌─────────────────────┐    ┌─────────────────────┐
│                     │    │                     │
│   Controlador de    │    │    PostgreSQL      │
│   Autenticación     │◄──►│ (Datos Usuario)     │
│                     │    │ (Refresh Tokens)    │
└─────────────────────┘    └─────────────────────┘
           │
           ▼
┌─────────────────────┐    ┌─────────────────────┐
│                     │    │                     │
│   Servicio de       │    │     Cassandra      │
│   Autenticación     │◄──►│ (Estado Votación)  │
│                     │    │                     │
└─────────────────────┘    └─────────────────────┘
           │
           ▼
┌─────────────────────┐
│                     │
│      Redis          │
│ (Blacklist Tokens)  │
│                     │
└─────────────────────┘
```

### Flujo de Datos

1. **Registro**: Usuario → Controlador → Servicio → PostgreSQL
2. **Autenticación**: Usuario → Controlador → AuthenticationManager → JWT + Refresh Token
3. **Refresh Token**: Refresh Token → Validación → Nuevo Access Token + Rotación
4. **Estado de Votación**: JWT → Controlador → Servicio → Cassandra → Respuesta
5. **Blacklist**: Logout → JTI Extraction → Redis Blacklist → Token Inválido

## Stack Tecnológico

### Tecnologías Principales
- **Java 17**: Lenguaje de programación principal
- **Spring Boot 3.x**: Framework de aplicación
- **Spring Security**: Framework de seguridad
- **Spring Data JPA**: Integración con PostgreSQL
- **Spring Data Cassandra**: Integración con Cassandra
- **Spring Data Redis**: Integración con Redis
- **Maven**: Automatización de construcción y gestión de dependencias

### Tecnologías de Seguridad
- **JWT (JSON Web Tokens)**: Tokens de autenticación con JTI
- **BCrypt**: Hash de contraseñas
- **Biblioteca JJWT 0.11.5**: Implementación JWT
- **Refresh Tokens**: Tokens de larga duración para renovación

### Tecnologías de Base de Datos
- **PostgreSQL**: Base de datos relacional para datos de usuario y refresh tokens
- **Cassandra**: Base de datos NoSQL para seguimiento de votos
- **Redis**: Cache para blacklist de tokens y gestión de sesiones

### Documentación y Pruebas
- **SpringDoc OpenAPI**: Documentación de API interactiva
- **JUnit 5**: Framework de pruebas unitarias
- **Mockito**: Framework de mocking para pruebas
- **Testcontainers**: Pruebas de integración con contenedores

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
  "userId": "123e4567-e89b-12d3-a456-426614174000",
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
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "refreshExpiresIn": 604800,
  "payload": {
    "userId": "123e4567-e89b-12d3-a456-426614174000",
    "email": "usuario@ejemplo.com",
    "role": "USER",
    "hasVoted": false
  }
}
```

#### Renovación de Token
```http
POST /api/v1/auth/refresh
Content-Type: application/json

{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "deviceInfo": "Mozilla/5.0..."
}
```

**Respuesta (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "new-refresh-token-uuid",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "email": "usuario@ejemplo.com",
  "role": "USER",
  "hasVoted": false,
  "timestamp": 1626284400000
}
```

#### Validación de Token
```http
POST /api/v1/auth/validate
Content-Type: application/json

{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Respuesta (200 OK):**
```json
{
  "valid": true,
  "timestamp": 1626284400000
}
```

#### Introspección de Token
```http
POST /api/v1/auth/payload
Content-Type: application/json

{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Respuesta (200 OK):**
```json
{
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "email": "usuario@ejemplo.com",
  "role": "USER",
  "hasVoted": false,
  "sub": "usuario@ejemplo.com",
  "iat": 1626284400,
  "exp": 1626370800,
  "jti": "unique-jwt-id"
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
    "refresh": "POST /api/v1/auth/refresh",
    "validate": "POST /api/v1/auth/validate",
    "payload": "POST /api/v1/auth/payload",
    "logout": "POST /api/v1/auth/logout",
    "votingStatus": "GET /api/v1/auth/voting-status (requiere JWT)",
    "markVoted": "POST /api/v1/auth/mark-voted (requiere JWT)",
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
  "email": "usuario@ejemplo.com",
  "role": "USER",
  "electionId": "550e8400-e29b-41d4-a716-446655440000",
  "eligible": true,
  "message": "El usuario es elegible para votar"
}
```

#### Marcar Usuario como Votado
```http
POST /api/v1/auth/mark-voted
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "electionId": "550e8400-e29b-41d4-a716-446655440000",
  "voteId": "vote-transaction-id"
}
```

**Respuesta (200 OK):**
```json
{
  "message": "Usuario marcado como votado exitosamente",
  "hasVoted": true,
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "electionId": "550e8400-e29b-41d4-a716-446655440000",
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "timestamp": 1626284400000
}
```

#### Cierre de Sesión
```http
POST /api/v1/auth/logout
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "refreshToken": "refresh-token-to-revoke"
}
```

**Respuesta (200 OK):**
```json
{
  "message": "Sesión cerrada exitosamente",
  "timestamp": 1626284400000
}
```

### Respuestas de Error

#### 400 Bad Request
```json
{
  "error": "Falló la validación",
  "message": "La contraseña debe contener al menos una letra mayúscula",
  "timestamp": 1626284400000
}
```

#### 401 Unauthorized
```json
{
  "error": "Falló la autenticación",
  "message": "Token inválido o expirado",
  "timestamp": 1626284400000
}
```

#### 409 Conflict
```json
{
  "error": "Conflicto de recurso",
  "message": "El usuario ya ha votado en esta elección",
  "timestamp": 1626284400000
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
- Redis 6.0+

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

#### PostgreSQL (application.yml)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/voting_db
    username: voting_user
    password: voting_password
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
```

#### Configuración de Cassandra
```yaml
spring:
  cassandra:
    contact-points: localhost
    port: 9042
    keyspace-name: voting_keyspace
    local-datacenter: datacenter1
    username: cassandra
    password: cassandra
```

#### Configuración de Redis
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      database: 0
      timeout: 2000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
```

### Configuración de Seguridad

#### Configuración JWT y Refresh Tokens
```yaml
jwt:
  secret: ${JWT_SECRET:your-256-bit-secret-key-here}
  expiration: 86400000  # 24 horas
  refresh-expiration: 604800000  # 7 días

auth:
  max-refresh-tokens-per-user: 5
  enable-device-tracking: true
  enable-token-rotation: true
```

#### Configuración BCrypt
```yaml
security:
  password:
    strength: 12
```

### Configuración de Aplicación

#### Configuración del Servidor
```yaml
server:
  port: 8081
  servlet:
    context-path: /

spring:
  application:
    name: auth-service
  task:
    scheduling:
      enabled: true

app:
  default-election-id: 550e8400-e29b-41d4-a716-446655440000
```

#### Configuración de Logging
```yaml
logging:
  level:
    com.auth: DEBUG
    org.springframework.security: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
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

# Iniciar Redis
docker run -d --name redis-auth \
  -p 6379:6379 redis:6.0-alpine
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
export SPRING_DATA_REDIS_HOST=redis-cluster
export JWT_SECRET=production-secret-key-256-bits
export SERVER_PORT=8081
```

#### Verificaciones de Salud
```bash
# Salud de la aplicación
curl http://localhost:8081/api/v1/auth/status

# Conectividad de base de datos
curl http://localhost:8081/actuator/health

# Verificar Redis
curl http://localhost:8081/actuator/health/redis
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
│   ├── VotingStatusServiceTest.java
│   ├── RefreshTokenServiceTest.java
│   └── TokenBlacklistServiceTest.java
├── controller/
│   └── AuthControllerTest.java
├── security/
│   ├── JwtUtilTest.java
│   └── filter/
│       └── JwtAuthFilterTest.java
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
# Importar colección actualizada
newman run docs/postman/auth-service-collection.json

# Ejecutar entorno específico
newman run docs/postman/auth-service-collection.json -e docs/postman/dev-environment.json
```

## Seguridad

### Flujo de Autenticación Avanzado

1. **Registro de Usuario**: Las contraseñas se hashean usando BCrypt con sal
2. **Proceso de Login**: Credenciales validadas contra la base de datos
3. **Generación de Tokens**: 
   - Access Token JWT firmado con algoritmo HS256 (24h)
   - Refresh Token seguro con UUID (7 días)
   - JTI único para cada token
4. **Validación de Token**: Verificación de firma, expiración y blacklist
5. **Renovación de Token**: Rotación automática de refresh tokens
6. **Revocación**: Blacklist basada en JTI almacenada en Redis
7. **Autorización**: Endpoints protegidos requieren JWT válido

### Seguridad de Refresh Tokens

- **Rotación Automática**: Cada uso genera un nuevo refresh token
- **Límite por Usuario**: Máximo 5 tokens activos por usuario
- **Tracking de Dispositivos**: Información de dispositivo e IP para auditoría
- **Limpieza Automática**: Tokens expirados eliminados cada hora
- **Revocación en Masa**: Invalidación de todos los tokens del usuario

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
- **JTI**: Verificación contra blacklist en Redis
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
curl http://localhost:8081/actuator/health/cassandra
curl http://localhost:8081/actuator/health/redis
```

### Logging Avanzado

#### Niveles de Log
- **ERROR**: Errores del sistema y excepciones
- **WARN**: Violaciones de seguridad y actividad sospechosa
- **INFO**: Eventos de autenticación, refresh tokens y lógica de negocio
- **DEBUG**: Información detallada de request/response

#### Formato de Log
```
2025-07-19 19:25:35 [http-nio-8081-exec-1] INFO  c.a.controller.AuthController - Login attempt for user: usuario@ejemplo.com | ip: 192.168.1.100
2025-07-19 19:25:35 [http-nio-8081-exec-1] INFO  c.a.service.RefreshTokenService - Created refresh token for user: usuario@ejemplo.com | tokenId: uuid | expiresAt: 2025-07-26T19:25:35 | ip: 192.168.1.100
2025-07-19 19:25:35 [http-nio-8081-exec-1] INFO  c.a.controller.AuthController - Login successful for user: usuario@ejemplo.com
```

### Métricas

#### Métricas de Rendimiento
- **Tiempo de Respuesta**: Promedio 10-150ms
- **Throughput**: Pool de hilos configurable
- **Uso de Memoria**: Configuración JVM optimizada
- **Conexiones de Base de Datos**: Monitoreo de pool de conexiones
- **Cache Redis**: Hit ratio y performance de blacklist

#### Métricas de Seguridad
- **Intentos de Login Fallidos**: Rastreados por usuario e IP
- **Fallas de Validación de Token**: Monitoreadas por actividad sospechosa
- **Refresh Token Usage**: Frecuencia de renovación de tokens
- **Blacklist Performance**: Efectividad de revocación de tokens
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
git commit -m "feat(auth): implementar refresh token rotation"

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
│   │   │   │   ├── RegisterRequest.java
│   │   │   │   ├── RefreshTokenRequest.java
│   │   │   │   ├── ValidateTokenRequest.java
│   │   │   │   ├── PayloadRequest.java
│   │   │   │   ├── LogoutRequest.java
│   │   │   │   └── MarkVotedRequest.java
│   │   │   ├── entity/
│   │   │   │   └── cassandra/
│   │   │   │       └── UserVoteLog.java
│   │   │   ├── exception/
│   │   │   │   └── TokenException.java
│   │   │   ├── model/
│   │   │   │   ├── User.java
│   │   │   │   └── RefreshToken.java
│   │   │   ├── repository/
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── RefreshTokenRepository.java
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
│   │   │       ├── RefreshTokenService.java
│   │   │       └── TokenBlacklistService.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── application-test.yml
│   └── test/
│       └── java/com/auth/
│           ├── controller/
│           │   └── AuthControllerTest.java
│           ├── service/
│           │   ├── AuthServiceTest.java
│           │   ├── RefreshTokenServiceTest.java
│           │   └── TokenBlacklistServiceTest.java
│           ├── security/
│           │   └── JwtUtilTest.java
│           └── integration/
│               └── AuthIntegrationTest.java
├── target/
├── Dockerfile
├── pom.xml
└── README.md
```

---

**Versión del Servicio**: 1.0.0  
**Última Actualización**: 19 de Julio, 2025  
**Mantenedor**: Equipo de Desarrollo  
**Licencia**: MIT

## Changelog

### v1.0.0
- ✅ Implementación completa de refresh tokens con rotación automática
- ✅ Sistema de blacklist de tokens con Redis
- ✅ JTI único para cada token JWT
- ✅ Endpoints avanzados: /refresh, /validate, /payload, /mark-voted
- ✅ Tracking de dispositivos e IPs para auditoría de seguridad
- ✅ Limpieza automática de tokens expirados
- ✅ Límites de seguridad (máximo 5 refresh tokens por usuario)
- ✅ Integración completa PostgreSQL + Cassandra + Redis
- ✅ Documentación API actualizada con Swagger UI
- ✅ Pruebas unitarias e integración con Testcontainers
- ✅ Configuración optimizada para producción
