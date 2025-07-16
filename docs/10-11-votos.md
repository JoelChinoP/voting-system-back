# 10. Descripción de la implementación

## 10.3 Microservicio de votos

El microservicio de votos es el componente central del sistema de votación electrónico, responsable de gestionar todas las operaciones relacionadas con la emisión, almacenamiento, validación y consulta de votos. Está desarrollado en Java 17 con Spring Boot 3.x, implementando una arquitectura híbrida que combina PostgreSQL para metadatos y Cassandra para el almacenamiento de votos, garantizando escalabilidad, disponibilidad y seguridad.

### Arquitectura técnica

**Stack tecnológico:**
- **Framework:** Spring Boot 3.x con Spring Security
- **Base de datos:** Arquitectura híbrida PostgreSQL + Apache Cassandra
- **Documentación:** OpenAPI/Swagger
- **Seguridad:** JWT (JSON Web Tokens)
- **Puerto:** 8083

**Arquitectura de datos:**
- **PostgreSQL:** Almacena metadatos como candidatos y estado de votación de usuarios
- **Cassandra:** Almacena los votos reales con alta disponibilidad y particionamiento
- **Entidades principales:**
  - `Candidate` (PostgreSQL): Información de candidatos
  - `UserVotingStatus` (PostgreSQL): Estado de votación por usuario
  - `Vote` (Cassandra): Registro de votos individuales
  - `VoteByCandidate` (Cassandra): Agregaciones por candidato
  - `UserVoteLog` (Cassandra): Log de actividad de votación

### Funcionalidades principales

**1. API de votación (`VoteController`):**
- **Endpoint principal:** `POST /api/v1/votes`
- **Autenticación:** Bearer Token JWT obligatorio
- **Validación:** DTO con validaciones Jakarta Bean Validation
- **Logging:** Registro detallado de todas las operaciones

**2. Emisión de votos:**
```java
@PostMapping("/api/v1/votes")
public ResponseEntity<VoteResponse> castVote(
    @Valid @RequestBody VoteRequest voteRequest,
    Authentication authentication)
```
- Valida la autenticidad del usuario mediante JWT
- Verifica que el usuario no haya votado previamente
- Registra el voto de forma transaccional en ambas bases de datos
- Implementa hash SHA-256 para anonimización

**3. Consulta de estado de votación:**
```java
@GetMapping("/status")
public ResponseEntity<VotingStatusResponse> getVotingStatus(
    Authentication authentication)
```
- Consulta si el usuario ya ha emitido su voto
- Proporciona información del estado actual de la votación

**4. Validación y seguridad:**
- **Prevención de doble voto:** Control mediante `UserVotingStatus` en PostgreSQL
- **Anonimización:** Hash del voto para proteger la privacidad
- **Transacciones:** Operaciones atómicas entre PostgreSQL y Cassandra
- **Auditoría:** Log completo de actividades en `UserVoteLog`

### Integración con otros servicios

**Comunicación inter-servicios:**
- **Microservicio de usuarios:** Validación de existencia y estado del usuario
- **Microservicio de autenticación:** Validación de tokens JWT y permisos
- **Microservicio de reportes:** Envío de datos agregados para análisis

**Patrones de integración:**
- **Comunicación síncrona:** REST APIs para validaciones en tiempo real
- **Comunicación asíncrona:** Eventos para notificaciones (implementación futura)
- **Circuit Breaker:** Resiliencia ante fallos de servicios externos

### Configuración y despliegue

**Variables de entorno principales:**
```properties
# Servidor
server.port=8083

# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/voting_system
spring.datasource.username=voting_user
spring.datasource.password=dev_password

# Cassandra
spring.cassandra.contact-points=localhost
spring.cassandra.port=9042
spring.cassandra.keyspace-name=voting_system
spring.cassandra.local-datacenter=datacenter1

# JWT
jwt.secret=a-string-secret-at-least-256-bits-long
jwt.expiration=86400

# Elección por defecto
app.default-election-id=550e8400-e29b-41d4-a716-446655440000
```

**Perfiles de configuración:**
- **Desarrollo:** Base de datos local, logging detallado
- **Producción:** Configuración optimizada, métricas habilitadas
- **Testing:** Base de datos en memoria, mocks activados

---

# 11. Demostración del funcionamiento de la implementación

Esta sección proporciona una guía completa para demostrar el funcionamiento del microservicio de votos, incluyendo escenarios de prueba, ejemplos de peticiones y respuestas esperadas.

## 11.1 Preparación del entorno

**1. Despliegue de infraestructura:**
```bash
# Levantar servicios con Docker Compose
docker-compose -f config/docker/docker-compose.yaml up -d

# Verificar servicios activos
docker-compose ps

# Verificar conectividad de bases de datos
docker exec -it postgres-container psql -U voting_user -d voting_system -c "SELECT 1;"
docker exec -it cassandra-container cqlsh -e "DESCRIBE KEYSPACE voting_system;"
```

**2. Verificación de salud del servicio:**
```bash
# Health check
curl -X GET http://localhost:8083/actuator/health

# Respuesta esperada:
{
  "status": "UP",
  "components": {
    "cassandra": {"status": "UP"},
    "db": {"status": "UP"}
  }
}
```

## 11.2 Escenarios de prueba

### Escenario 1: Emisión exitosa de voto

**Paso 1:** Obtener token JWT (desde microservicio de autenticación)
```bash
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "voter123",
    "password": "password123"
  }'

# Respuesta:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 86400
}
```

**Paso 2:** Emitir voto
```bash
curl -X POST http://localhost:8083/api/v1/votes \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "candidateId": "550e8400-e29b-41d4-a716-446655440001"
  }'

# Respuesta exitosa:
{
  "success": true,
  "message": "Voto registrado exitosamente",
  "voteId": "123e4567-e89b-12d3-a456-426614174000",
  "timestamp": "2025-07-16T10:30:00Z"
}
```

### Escenario 2: Prevención de voto duplicado

**Intento de segundo voto:**
```bash
curl -X POST http://localhost:8083/api/v1/votes \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "candidateId": "550e8400-e29b-41d4-a716-446655440002"
  }'

# Respuesta de error:
{
  "success": false,
  "error": "USER_ALREADY_VOTED",
  "message": "El usuario ya ha emitido su voto",
  "timestamp": "2025-07-16T10:35:00Z"
}
```

### Escenario 3: Consulta de estado de votación

```bash
curl -X GET http://localhost:8083/api/v1/votes/status \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# Respuesta para usuario que ya votó:
{
  "hasVoted": true,
  "votingTimestamp": "2025-07-16T10:30:00Z",
  "electionId": "550e8400-e29b-41d4-a716-446655440000"
}

# Respuesta para usuario que no ha votado:
{
  "hasVoted": false,
  "electionId": "550e8400-e29b-41d4-a716-446655440000"
}
```

## 11.3 Validación de integridad de datos

**1. Verificación en PostgreSQL:**
```sql
-- Verificar estado de votación
SELECT user_id, has_voted, voting_timestamp 
FROM user_voting_status 
WHERE user_id = '123e4567-e89b-12d3-a456-426614174000';
```

**2. Verificación en Cassandra:**
```cql
-- Verificar voto registrado
SELECT vote_id, candidate_id, vote_timestamp 
FROM votes 
WHERE election_id = 550e8400-e29b-41d4-a716-446655440000;

-- Verificar agregaciones
SELECT candidate_id, vote_count 
FROM votes_by_candidate 
WHERE election_id = 550e8400-e29b-41d4-a716-446655440000;
```

## 11.4 Pruebas de rendimiento y escalabilidad

**Prueba de carga básica:**
```bash
# Usando Apache Bench
ab -n 1000 -c 10 -H "Authorization: Bearer <token>" \
   -H "Content-Type: application/json" \
   -p vote-payload.json \
   http://localhost:8083/api/v1/votes

# Métricas esperadas:
# - Tiempo de respuesta promedio: < 200ms
# - Throughput: > 100 requests/second
# - Tasa de error: 0%
```

## 11.5 Herramientas de monitoreo

**Logs de aplicación:**
```bash
# Seguimiento de logs en tiempo real
docker logs -f votes-service-container

# Búsqueda de errores
docker logs votes-service-container 2>&1 | grep ERROR
```

**Métricas de Actuator:**
```bash
# Métricas generales
curl http://localhost:8083/actuator/metrics

# Métricas específicas de base de datos
curl http://localhost:8083/actuator/metrics/hikaricp.connections.active
curl http://localhost:8083/actuator/metrics/cassandra.request.latency
```

## 11.6 Colección de Postman

Se recomienda crear una colección de Postman con los siguientes elementos:
- Variables de entorno (URLs, tokens)
- Pre-request scripts para autenticación automática
- Tests de validación de respuestas
- Escenarios de flujo completo (login → vote → status)

Esta demostración integral permite validar tanto la funcionalidad básica como los aspectos avanzados de seguridad, rendimiento y monitoreo del microservicio de votos.
