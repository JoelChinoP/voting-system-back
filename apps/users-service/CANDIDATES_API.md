# Sistema de Votación - Users Service (Candidates API)

## Descripción
Este microservicio maneja la gestión de candidatos en el sistema de votación. Proporciona endpoints REST para crear, leer, actualizar y eliminar candidatos.

## Configuración

### Base de datos
El servicio utiliza PostgreSQL. Asegúrate de tener una base de datos llamada `voting_system` ejecutándose en `localhost:5432`.

### Configuración de desarrollo
El archivo `application-dev.properties` contiene la configuración para desarrollo con:
- Puerto: 8081
- Base de datos: PostgreSQL local
- JWT configurado
- CORS habilitado para frontend en desarrollo

## Endpoints API

### Candidatos

#### GET /api/v1/candidates
Obtiene todos los candidatos registrados.
- **Autenticación**: Requerida (USER o ADMIN)
- **Respuesta**: Lista de candidatos

#### GET /api/v1/candidates/{id}
Obtiene un candidato específico por ID.
- **Autenticación**: Requerida (USER o ADMIN)
- **Parámetros**: `id` - ID del candidato
- **Respuesta**: Datos del candidato

#### POST /api/v1/candidates
Crea un nuevo candidato.
- **Autenticación**: Requerida (ADMIN)
- **Body**: Datos del candidato
- **Respuesta**: Candidato creado

#### PUT /api/v1/candidates/{id}
Actualiza un candidato existente.
- **Autenticación**: Requerida (ADMIN)
- **Parámetros**: `id` - ID del candidato
- **Body**: Datos actualizados
- **Respuesta**: Candidato actualizado

#### DELETE /api/v1/candidates/{id}
Elimina un candidato.
- **Autenticación**: Requerida (ADMIN)
- **Parámetros**: `id` - ID del candidato
- **Respuesta**: 204 No Content

#### GET /api/v1/candidates/partido/{partido}
Obtiene candidatos por partido político.
- **Autenticación**: Requerida (USER o ADMIN)
- **Parámetros**: `partido` - Nombre del partido
- **Respuesta**: Lista de candidatos del partido

#### GET /api/v1/candidates/search?nombre={nombre}
Busca candidatos por nombre.
- **Autenticación**: Requerida (USER o ADMIN)
- **Parámetros**: `nombre` - Término de búsqueda
- **Respuesta**: Lista de candidatos que coinciden

## Modelo de datos

### CandidateRequest
```json
{
  "nombre": "string (requerido)",
  "partidoPolitico": "string (requerido)",
  "cargo": "string (opcional)",
  "color": "string (opcional)",
  "propuestas": "string (opcional)",
  "experiencia": "string (opcional)",
  "descripcion": "string (opcional)",
  "email": "string (opcional)",
  "telefono": "string (opcional)",
  "lugarNacimiento": "string (opcional)",
  "educacion": "string (opcional)",
  "sitioWeb": "string (opcional)",
  "imagen": "string (opcional)"
}
```

### CandidateResponse
```json
{
  "id": "number",
  "nombre": "string",
  "partidoPolitico": "string",
  "cargo": "string",
  "color": "string",
  "propuestas": "string",
  "experiencia": "string",
  "descripcion": "string",
  "email": "string",
  "telefono": "string",
  "lugarNacimiento": "string",
  "educacion": "string",
  "sitioWeb": "string",
  "imagen": "string",
  "createdAt": "datetime",
  "updatedAt": "datetime"
}
```

## Integración con Frontend

El frontend (React/TypeScript) está configurado para usar estos endpoints:
- URL base: `http://localhost:8081`
- Autenticación: Bearer token en header Authorization
- Mapeo automático entre tipos frontend y backend

### Mapeo de campos
- Frontend `name` ↔ Backend `nombre`
- Frontend `party` ↔ Backend `partidoPolitico`  
- Frontend `position` ↔ Backend `cargo`
- Frontend `proposals` (array) ↔ Backend `propuestas` (string separado por comas)
- Frontend `experience` ↔ Backend `experiencia`
- Frontend `description` ↔ Backend `descripcion`
- Frontend `phone` ↔ Backend `telefono`
- Frontend `birthPlace` ↔ Backend `lugarNacimiento`
- Frontend `education` ↔ Backend `educacion`
- Frontend `website` ↔ Backend `sitioWeb`
- Frontend `image/avatar` ↔ Backend `imagen`

## Ejecución

### Desarrollo
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Producción
```bash
./mvnw clean package
java -jar target/users-0.0.1-SNAPSHOT.jar
```

## Documentación API
Una vez ejecutado el servicio, la documentación Swagger estará disponible en:
- http://localhost:8081/swagger-ui.html
- http://localhost:8081/v3/api-docs
