package com.users.controller;

import com.users.dto.CandidateRequest;
import com.users.dto.CandidateResponse;
import com.users.service.CandidateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(
        origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:4173"},
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS},
        allowCredentials = "true"
)
@RestController
@RequestMapping("/api/v1/candidates")
@Tag(name = "Candidates", description = "Candidate management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class CandidateController {

    private static final Logger log = LoggerFactory.getLogger(CandidateController.class);
    private final CandidateService candidateService;

    public CandidateController(CandidateService candidateService) {
        this.candidateService = candidateService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Crear nuevo candidato",
        description = "Crea un nuevo candidato en el sistema. Solo accesible para administradores."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Candidato creado exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CandidateResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos de entrada inválidos",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "401",
            description = "No autorizado - Token JWT inválido o ausente",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acceso denegado - Se requiere rol ADMIN",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "409",
            description = "El candidato ya existe",
            content = @Content(mediaType = "application/json")
        )
    })
    public ResponseEntity<CandidateResponse> createCandidate(
            @Valid @RequestBody 
            @Parameter(description = "Datos del candidato a crear", required = true)
            CandidateRequest request) {

        log.info("Solicitud para crear candidato: {}", request.getNombre());
        CandidateResponse response = candidateService.createCandidate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(
        summary = "Obtener todos los candidatos",
        description = "Obtiene la lista completa de candidatos registrados. Accesible para usuarios con rol USER o ADMIN."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de candidatos obtenida exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CandidateResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "No autorizado - Token JWT inválido o ausente",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acceso denegado - Se requiere rol USER o ADMIN",
            content = @Content(mediaType = "application/json")
        )
    })
    public ResponseEntity<List<CandidateResponse>> getAllCandidates() {
        log.info("Solicitud para obtener todos los candidatos");
        List<CandidateResponse> candidates = candidateService.getAllCandidates();
        return ResponseEntity.ok(candidates);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(
        summary = "Obtener candidato por ID",
        description = "Obtiene los detalles de un candidato específico por su ID. Accesible para usuarios con rol USER o ADMIN."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Candidato obtenido exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CandidateResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "No autorizado - Token JWT inválido o ausente",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acceso denegado - Se requiere rol USER o ADMIN",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Candidato no encontrado",
            content = @Content(mediaType = "application/json")
        )
    })
    public ResponseEntity<CandidateResponse> getCandidateById(
            @PathVariable 
            @Parameter(description = "ID del candidato", required = true, example = "1")
            Long id) {

        log.info("Solicitud para obtener candidato con ID: {}", id);
        CandidateResponse candidate = candidateService.getCandidateById(id);
        return ResponseEntity.ok(candidate);
    }

    @GetMapping("/partido/{partido}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(
        summary = "Obtener candidatos por partido político",
        description = "Obtiene todos los candidatos de un partido político específico. Accesible para usuarios con rol USER o ADMIN."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Candidatos del partido obtenidos exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CandidateResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "No autorizado - Token JWT inválido o ausente",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acceso denegado - Se requiere rol USER o ADMIN",
            content = @Content(mediaType = "application/json")
        )
    })
    public ResponseEntity<List<CandidateResponse>> getCandidatesByPartido(
            @PathVariable
            @Parameter(description = "Nombre del partido político", required = true, example = "Partido Liberal")
            String partido) {

        log.info("Solicitud para obtener candidatos del partido: {}", partido);
        List<CandidateResponse> candidates = candidateService.getCandidatesByPartido(partido);
        return ResponseEntity.ok(candidates);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(
        summary = "Buscar candidatos por nombre",
        description = "Busca candidatos por nombre (búsqueda parcial). Accesible para usuarios con rol USER o ADMIN."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Resultados de búsqueda obtenidos exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CandidateResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "No autorizado - Token JWT inválido o ausente",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acceso denegado - Se requiere rol USER o ADMIN",
            content = @Content(mediaType = "application/json")
        )
    })
    public ResponseEntity<List<CandidateResponse>> searchCandidatesByName(
            @RequestParam
            @Parameter(description = "Nombre a buscar", required = true, example = "Juan")
            String nombre) {

        log.info("Solicitud para buscar candidatos por nombre: {}", nombre);
        List<CandidateResponse> candidates = candidateService.searchCandidatesByName(nombre);
        return ResponseEntity.ok(candidates);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Actualizar candidato",
        description = "Actualiza los datos de un candidato existente. Solo accesible para administradores."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Candidato actualizado exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CandidateResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos de entrada inválidos",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "401",
            description = "No autorizado - Token JWT inválido o ausente",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acceso denegado - Se requiere rol ADMIN",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Candidato no encontrado",
            content = @Content(mediaType = "application/json")
        )
    })
    public ResponseEntity<CandidateResponse> updateCandidate(
            @PathVariable 
            @Parameter(description = "ID del candidato", required = true, example = "1")
            Long id,
            @Valid @RequestBody 
            @Parameter(description = "Datos actualizados del candidato", required = true)
            CandidateRequest request) {

        log.info("Solicitud para actualizar candidato con ID: {}", id);
        CandidateResponse response = candidateService.updateCandidate(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Eliminar candidato",
        description = "Elimina un candidato del sistema. Solo accesible para administradores."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Candidato eliminado exitosamente"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "No autorizado - Token JWT inválido o ausente",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acceso denegado - Se requiere rol ADMIN",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Candidato no encontrado",
            content = @Content(mediaType = "application/json")
        )
    })
    public ResponseEntity<Void> deleteCandidate(
            @PathVariable 
            @Parameter(description = "ID del candidato", required = true, example = "1")
            Long id) {

        log.info("Solicitud para eliminar candidato con ID: {}", id);
        candidateService.deleteCandidate(id);
        return ResponseEntity.noContent().build();
    }
}
