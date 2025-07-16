# Reports Service

Microservicio de reportes para el sistema de votación electrónica distribuida.

## Descripción

El `reports-service` es responsable de generar y proporcionar reportes relacionados con los resultados de las votaciones. Este servicio forma parte de la arquitectura de microservicios del sistema de votación electrónica y se comunica con otros servicios para obtener los datos necesarios para generar reportes precisos y actualizados.

## Características Principales

- **Reportes de Votos por Candidato**: Proporciona el número de votos recibidos por cada candidato
- **Resultados Generales**: Genera resúmenes completos de la votación incluyendo ganador y estadísticas
- **Autenticación JWT**: Todos los endpoints requieren autenticación mediante tokens JWT
- **Documentación OpenAPI**: API completamente documentada con Swagger
- **Manejo de Errores**: Sistema robusto de manejo de excepciones
- **Pruebas Unitarias**: Cobertura completa de pruebas para servicios y controladores

## Tecnologías Utilizadas

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Security** (para autenticación JWT)
- **Spring Web** (para APIs REST)
- **OpenAPI 3** (documentación de API)
- **JUnit 5** (pruebas unitarias)
- **Mockito** (mocking para pruebas)
- **Maven** (gestión de dependencias)

## Estructura del Proyecto

```
reports-service/
├── src/
│   ├── main/
│   │   ├── java/com/reports/
│   │   │   ├── config/           # Configuraciones (Security, OpenAPI)
│   │   │   ├── controller/       # Controladores REST
│   │   │   ├── dto/              # Data Transfer Objects
│   │   │   ├── exception/        # Manejo de excepciones
│   │   │   ├── security/         # Componentes de seguridad JWT
│   │   │   ├── service/          # Lógica de negocio
│   │   │   └── ReportsServiceApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       ├── java/com/reports/
│       │   ├── controller/       # Pruebas de controladores
│       │   ├── service/          # Pruebas de servicios
│       │   └── ReportsServiceApplicationTests.java
│       └── resources/
│           └── application-test.properties
├── pom.xml
└── README.md
```

## Configuración

### Variables de Entorno

El servicio utiliza las siguientes configuraciones principales:

```properties
# Puerto del servidor
server.port=8084

# URLs de otros microservicios
votes.service.url=http://localhost:8083
users.service.url=http://localhost:8082
auth.service.url=http://localhost:8081

# Configuración JWT
jwt.secret=mySecretKey
jwt.expiration=86400000
```

### Dependencias de Otros Servicios

El `reports-service` depende de los siguientes microservicios:

- **votes-service** (puerto 8083): Para obtener datos de votación
- **users-service** (puerto 8082): Para obtener estadísticas de usuarios
- **auth-service** (puerto 8081): Para validación de tokens JWT

## API Endpoints

### Autenticación

Todos los endpoints (excepto `/health`) requieren un token JWT válido en el header `Authorization`:

```
Authorization: Bearer <jwt-token>
```

### Endpoints Disponibles

#### 1. Obtener Votos por Candidato

```http
GET /api/v1/reports/votes-by-candidate
```

**Respuesta:**
```json
{
  "success": true,
  "message": "Reporte de votos por candidato generado exitosamente",
  "data": [
    {
      "candidateId": "1",
      "candidateName": "Juan Pérez",
      "voteCount": 150,
      "percentage": 60.0
    }
  ],
  "timestamp": "2025-01-16T10:30:00"
}
```

#### 2. Obtener Resultados Generales

```http
GET /api/v1/reports/overall-results
```

**Respuesta:**
```json
{
  "success": true,
  "message": "Reporte de resultados generales generado exitosamente",
  "data": {
    "winningCandidateId": "1",
    "winningCandidateName": "Juan Pérez",
    "totalVotesCast": 250,
    "totalRegisteredUsers": 500,
    "participationPercentage": 50.0,
    "votingStatus": "ACTIVE",
    "candidateResults": [...],
    "reportGeneratedAt": "2025-01-16T10:30:00"
  },
  "timestamp": "2025-01-16T10:30:00"
}
```

#### 3. Health Check

```http
GET /api/v1/reports/health
```

#### 4. Refrescar Datos

```http
POST /api/v1/reports/refresh
```

## Instalación y Ejecución

### Prerrequisitos

- Java 17 o superior
- Maven 3.6 o superior
- Acceso a los otros microservicios del sistema

### Compilación

```bash
mvn clean compile
```

### Ejecución de Pruebas

```bash
mvn test
```

### Ejecución del Servicio

```bash
mvn spring-boot:run
```

El servicio estará disponible en `http://localhost:8084`

### Empaquetado

```bash
mvn clean package
```

Esto generará un archivo JAR ejecutable en `target/reports-service-0.0.1-SNAPSHOT.jar`

## Documentación de la API

Una vez que el servicio esté ejecutándose, la documentación interactiva de la API estará disponible en:

- **Swagger UI**: http://localhost:8084/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8084/v3/api-docs

## Monitoreo

El servicio incluye endpoints de Actuator para monitoreo:

- **Health**: http://localhost:8084/actuator/health
- **Info**: http://localhost:8084/actuator/info
- **Metrics**: http://localhost:8084/actuator/metrics

## Manejo de Errores

El servicio implementa un manejo global de excepciones que proporciona respuestas consistentes:

```json
{
  "success": false,
  "message": "Descripción del error",
  "errorCode": "CODIGO_ERROR",
  "timestamp": "2025-01-16T10:30:00"
}
```

### Códigos de Error Comunes

- `VOTES_SERVICE_ERROR`: Error al comunicarse con el servicio de votos
- `VOTES_SERVICE_NO_DATA`: No se recibieron datos del servicio de votos
- `REPORT_GENERATION_ERROR`: Error general en la generación de reportes
- `INVALID_CREDENTIALS`: Token JWT inválido o expirado
- `ACCESS_DENIED`: Sin permisos para acceder al recurso

## Desarrollo

### Agregar Nuevos Reportes

1. Crear un nuevo DTO en el paquete `dto`
2. Agregar la lógica de negocio en `ReportService`
3. Crear un nuevo endpoint en `ReportController`
4. Agregar pruebas unitarias correspondientes

### Configuración de Desarrollo

Para desarrollo local, puedes crear un archivo `application-dev.properties`:

```properties
# Configuración de desarrollo
logging.level.com.reports=DEBUG
votes.service.url=http://localhost:8083
users.service.url=http://localhost:8082
```

Y ejecutar con el perfil de desarrollo:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## Contribución

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/nueva-funcionalidad`)
3. Commit tus cambios (`git commit -am 'Agregar nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Crea un Pull Request

## Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo `LICENSE` para más detalles.

## Contacto

Para preguntas o soporte, contacta al equipo de desarrollo:

- Email: desarrollo@votacion.com
- Documentación: [Wiki del Proyecto]
- Issues: [GitHub Issues]

