# Auth Service

Microservicio de autenticación y autorización para el sistema de votación.

## Funcionalidades

### Requerimientos Funcionales Implementados

- **RF-001: Registro de usuarios**
  - Validación de nombre completo (2-100 caracteres)
  - Validación de email único
  - Validación de contraseña (≥8 caracteres, mayúscula, minúscula, número)
  - Cifrado con BCrypt (factor 12)

- **RF-002: Autenticación de usuarios**
  - Login con email y contraseña
  - Generación de JWT firmado con HS256
  - Expiración de 24 horas

- **RF-003: Verificación de elegibilidad para votar**
  - Consulta a Cassandra para verificar si el usuario ya votó
  - Endpoint `/api/v1/auth/voting-status`

## Endpoints

### Públicos
- `POST /api/v1/auth/register` - Registro de usuario
- `POST /api/v1/auth/login` - Autenticación
- `GET /api/v1/auth/status` - Estado del servicio

### Protegidos (requieren JWT)
- `GET /api/v1/auth/voting-status` - Verificación de elegibilidad
- `GET /api/v1/auth/has-voted` - Endpoint legacy (deprecated)

## Configuración

### Base de Datos
- **PostgreSQL**: Almacenamiento de usuarios
- **Cassandra**: Consulta de estado de votación

### Seguridad
- JWT con algoritmo HS256
- Contraseñas con BCrypt (cost factor 12)
- CORS habilitado
- Sesiones stateless

## Tecnologías

- Java 17
- Spring Boot 3.x
- Spring Security
- Spring Data JPA
- Spring Data Cassandra
- JWT (jjwt)
- PostgreSQL
- Cassandra
- Swagger/OpenAPI

## Ejecución

```bash
# Compilar
./mvnw clean compile

# Ejecutar
./mvnw spring-boot:run

# Ejecutar tests
./mvnw test
```

## Documentación API

La documentación Swagger está disponible en:
- http://localhost:8081/swagger-ui.html
- http://localhost:8081/v3/api-docs

## Estructura del Proyecto

```
src/
├── main/
│   ├── java/com/auth/
│   │   ├── config/           # Configuraciones
│   │   ├── controller/       # Controladores REST
│   │   ├── dto/             # DTOs de entrada/salida
│   │   ├── entity/          # Entidades JPA y Cassandra
│   │   ├── exception/       # Manejo de excepciones
│   │   ├── model/           # Modelos de dominio
│   │   ├── repository/      # Repositorios
│   │   ├── security/        # Configuración de seguridad
│   │   └── service/         # Servicios de negocio
│   └── resources/
│       └── application.properties
└── test/
    └── java/com/auth/
        └── service/         # Tests unitarios
```

## Validaciones

### Registro de Usuario
- Nombre completo: 2-100 caracteres
- Email: formato válido y único
- Contraseña: mínimo 8 caracteres, al menos una mayúscula, minúscula y número

### Respuestas de Error
- 400: Datos de entrada inválidos
- 401: Credenciales inválidas
- 409: Email ya registrado

## Compatibilidad

Este servicio es compatible con:
- votes-service (consulta de estado de votación)
- reports-service (autenticación)
- users-service (autenticación)

## Monitoreo

El servicio incluye:
- Endpoint de health check: `/api/v1/auth/status`
- Logs estructurados con SLF4J
- Validación de tokens JWT
- Manejo global de excepciones
