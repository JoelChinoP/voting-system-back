package com.reports.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * DTO genérico para respuestas de la API.
 * 
 * Esta clase proporciona una estructura estándar para todas las respuestas
 * de la API, incluyendo información sobre el éxito de la operación,
 * mensajes y metadatos.
 * 
 * @param <T> Tipo de datos contenidos en la respuesta
 */
@Schema(description = "Respuesta estándar de la API")
public class ApiResponse<T> {

    @Schema(description = "Indica si la operación fue exitosa", example = "true")
    @JsonProperty("success")
    private boolean success;

    @Schema(description = "Mensaje descriptivo de la respuesta", example = "Operación completada exitosamente")
    @JsonProperty("message")
    private String message;

    @Schema(description = "Datos de la respuesta")
    @JsonProperty("data")
    private T data;

    @Schema(description = "Código de error (si aplica)", example = "REPORT_001")
    @JsonProperty("errorCode")
    private String errorCode;

    @Schema(description = "Timestamp de la respuesta")
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    /**
     * Constructor por defecto.
     */
    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Constructor para respuesta exitosa con datos.
     * 
     * @param data Datos de la respuesta
     */
    public ApiResponse(T data) {
        this.success = true;
        this.message = "Operación completada exitosamente";
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Constructor para respuesta exitosa con mensaje personalizado.
     * 
     * @param data Datos de la respuesta
     * @param message Mensaje personalizado
     */
    public ApiResponse(T data, String message) {
        this.success = true;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Constructor para respuesta de error.
     * 
     * @param message Mensaje de error
     * @param errorCode Código de error
     */
    public ApiResponse(String message, String errorCode) {
        this.success = false;
        this.message = message;
        this.errorCode = errorCode;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Método estático para crear una respuesta exitosa.
     * 
     * @param data Datos de la respuesta
     * @param <T> Tipo de datos
     * @return Respuesta exitosa
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data);
    }

    /**
     * Método estático para crear una respuesta exitosa con mensaje.
     * 
     * @param data Datos de la respuesta
     * @param message Mensaje personalizado
     * @param <T> Tipo de datos
     * @return Respuesta exitosa
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(data, message);
    }

    /**
     * Método estático para crear una respuesta de error.
     * 
     * @param message Mensaje de error
     * @param errorCode Código de error
     * @param <T> Tipo de datos
     * @return Respuesta de error
     */
    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return new ApiResponse<>(message, errorCode);
    }

    // Getters y Setters

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", data=" + data +
                ", errorCode='" + errorCode + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}

