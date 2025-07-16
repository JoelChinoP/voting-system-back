package com.reports.controller;

import com.reports.dto.ApiResponse;
import com.reports.dto.CandidateVotesDTO;
import com.reports.dto.OverallResultsDTO;
import com.reports.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para el manejo de reportes de votación.
 * 
 * Este controlador expone los endpoints para consultar diferentes tipos
 * de reportes relacionados con los resultados de las votaciones.
 */
@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Reports", description = "API para la gestión de reportes de votación")
@SecurityRequirement(name = "Bearer Authentication")
public class ReportController {

    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

    @Autowired
    private ReportService reportService;

    /**
     * Obtiene el número de votos por cada candidato.
     * 
     * @return Lista de candidatos con sus respectivos conteos de votos
     */
    @GetMapping("/votes-by-candidate")
    @Operation(
        summary = "Obtener votos por candidato",
        description = "Retorna el número de votos recibidos por cada candidato en la votación actual"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Lista de votos por candidato obtenida exitosamente"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "Token de autenticación inválido o no proporcionado"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", 
            description = "No tiene permisos para acceder a este recurso"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500", 
            description = "Error interno del servidor al generar el reporte"
        )
    })
    public ResponseEntity<ApiResponse<List<CandidateVotesDTO>>> getVotesByCandidate() {
        logger.info("Solicitud recibida para obtener votos por candidato");
        
        try {
            List<CandidateVotesDTO> candidateVotes = reportService.getVotesByCandidate();
            
            logger.info("Reporte de votos por candidato generado exitosamente. {} candidatos encontrados", 
                       candidateVotes.size());
            
            ApiResponse<List<CandidateVotesDTO>> response = ApiResponse.success(
                candidateVotes, 
                "Reporte de votos por candidato generado exitosamente"
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error al generar reporte de votos por candidato: {}", e.getMessage(), e);
            throw e; // El GlobalExceptionHandler se encargará de manejar la excepción
        }
    }

    /**
     * Obtiene un resumen general de los resultados de la votación.
     * 
     * @return Resultados generales incluyendo ganador, participación y estadísticas
     */
    @GetMapping("/overall-results")
    @Operation(
        summary = "Obtener resultados generales",
        description = "Retorna un resumen completo de los resultados de la votación, incluyendo el candidato ganador, participación total y estadísticas generales"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Resultados generales obtenidos exitosamente"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "Token de autenticación inválido o no proporcionado"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", 
            description = "No tiene permisos para acceder a este recurso"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500", 
            description = "Error interno del servidor al generar el reporte"
        )
    })
    public ResponseEntity<ApiResponse<OverallResultsDTO>> getOverallResults() {
        logger.info("Solicitud recibida para obtener resultados generales");
        
        try {
            OverallResultsDTO overallResults = reportService.getOverallResults();
            
            logger.info("Reporte de resultados generales generado exitosamente. Total de votos: {}, Ganador: {}", 
                       overallResults.getTotalVotesCast(), 
                       overallResults.getWinningCandidateName());
            
            ApiResponse<OverallResultsDTO> response = ApiResponse.success(
                overallResults, 
                "Reporte de resultados generales generado exitosamente"
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error al generar reporte de resultados generales: {}", e.getMessage(), e);
            throw e; // El GlobalExceptionHandler se encargará de manejar la excepción
        }
    }

    /**
     * Endpoint de health check para verificar el estado del servicio.
     * 
     * @return Estado del servicio
     */
    @GetMapping("/health")
    @Operation(
        summary = "Health check del servicio",
        description = "Verifica que el servicio de reportes esté funcionando correctamente"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Servicio funcionando correctamente"
        )
    })
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        logger.debug("Health check solicitado");
        
        ApiResponse<String> response = ApiResponse.success(
            "OK", 
            "Servicio de reportes funcionando correctamente"
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para refrescar/actualizar los datos de reportes.
     * Este endpoint puede ser útil para forzar una actualización de los datos
     * desde los servicios externos.
     * 
     * @return Confirmación de actualización
     */
    @PostMapping("/refresh")
    @Operation(
        summary = "Refrescar datos de reportes",
        description = "Fuerza una actualización de los datos de reportes desde los servicios externos"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Datos actualizados exitosamente"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "Token de autenticación inválido o no proporcionado"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", 
            description = "No tiene permisos para realizar esta operación"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500", 
            description = "Error interno del servidor al actualizar los datos"
        )
    })
    public ResponseEntity<ApiResponse<String>> refreshReports() {
        logger.info("Solicitud recibida para refrescar datos de reportes");
        
        try {
            // Realizar una consulta para forzar la actualización de datos
            reportService.getOverallResults();
            
            logger.info("Datos de reportes actualizados exitosamente");
            
            ApiResponse<String> response = ApiResponse.success(
                "Actualizado", 
                "Datos de reportes actualizados exitosamente"
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error al actualizar datos de reportes: {}", e.getMessage(), e);
            throw e; // El GlobalExceptionHandler se encargará de manejar la excepción
        }
    }
}

