package com.reports.exception;

import com.reports.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

/**
 * Manejador global de excepciones para el microservicio de reportes.
 * 
 * Esta clase centraliza el manejo de excepciones en toda la aplicación,
 * proporcionando respuestas consistentes y apropiadas para diferentes
 * tipos de errores.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maneja excepciones de generación de reportes.
     * 
     * @param ex Excepción de generación de reportes
     * @param request Solicitud web
     * @return Respuesta de error
     */
    @ExceptionHandler(ReportGenerationException.class)
    public ResponseEntity<ApiResponse<Object>> handleReportGenerationException(
            ReportGenerationException ex, WebRequest request) {
        
        logger.error("Error en la generación de reportes: {}", ex.getMessage(), ex);
        
        ApiResponse<Object> response = ApiResponse.error(
            ex.getMessage(), 
            ex.getErrorCode() != null ? ex.getErrorCode() : "REPORT_GENERATION_ERROR"
        );
        
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Maneja excepciones de validación de argumentos.
     * 
     * @param ex Excepción de validación
     * @param request Solicitud web
     * @return Respuesta de error
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        logger.warn("Error de validación: {}", ex.getMessage());
        
        StringBuilder errorMessage = new StringBuilder("Errores de validación: ");
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errorMessage.append(error.getField())
                       .append(" - ")
                       .append(error.getDefaultMessage())
                       .append("; ")
        );
        
        ApiResponse<Object> response = ApiResponse.error(
            errorMessage.toString(), 
            "VALIDATION_ERROR"
        );
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja excepciones de acceso denegado.
     * 
     * @param ex Excepción de acceso denegado
     * @param request Solicitud web
     * @return Respuesta de error
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {
        
        logger.warn("Acceso denegado: {}", ex.getMessage());
        
        ApiResponse<Object> response = ApiResponse.error(
            "No tiene permisos para acceder a este recurso", 
            "ACCESS_DENIED"
        );
        
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    /**
     * Maneja excepciones de credenciales incorrectas.
     * 
     * @param ex Excepción de credenciales incorrectas
     * @param request Solicitud web
     * @return Respuesta de error
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadCredentialsException(
            BadCredentialsException ex, WebRequest request) {
        
        logger.warn("Credenciales incorrectas: {}", ex.getMessage());
        
        ApiResponse<Object> response = ApiResponse.error(
            "Token de autenticación inválido o expirado", 
            "INVALID_CREDENTIALS"
        );
        
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Maneja excepciones de argumentos ilegales.
     * 
     * @param ex Excepción de argumento ilegal
     * @param request Solicitud web
     * @return Respuesta de error
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        
        logger.warn("Argumento ilegal: {}", ex.getMessage());
        
        ApiResponse<Object> response = ApiResponse.error(
            ex.getMessage(), 
            "INVALID_ARGUMENT"
        );
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja todas las demás excepciones no específicas.
     * 
     * @param ex Excepción general
     * @param request Solicitud web
     * @return Respuesta de error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(
            Exception ex, WebRequest request) {
        
        logger.error("Error interno del servidor: {}", ex.getMessage(), ex);
        
        ApiResponse<Object> response = ApiResponse.error(
            "Ha ocurrido un error interno del servidor", 
            "INTERNAL_SERVER_ERROR"
        );
        
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

