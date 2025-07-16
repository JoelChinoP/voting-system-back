package com.reports.exception;

/**
 * Excepción personalizada para errores en la generación de reportes.
 * 
 * Esta excepción se lanza cuando ocurre un error durante el proceso
 * de generación de reportes, como problemas de conectividad con
 * servicios externos o errores en el procesamiento de datos.
 */
public class ReportGenerationException extends RuntimeException {

    private String errorCode;

    /**
     * Constructor con mensaje.
     * 
     * @param message Mensaje de error
     */
    public ReportGenerationException(String message) {
        super(message);
    }

    /**
     * Constructor con mensaje y causa.
     * 
     * @param message Mensaje de error
     * @param cause Causa del error
     */
    public ReportGenerationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor con mensaje y código de error.
     * 
     * @param message Mensaje de error
     * @param errorCode Código de error
     */
    public ReportGenerationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Constructor completo.
     * 
     * @param message Mensaje de error
     * @param cause Causa del error
     * @param errorCode Código de error
     */
    public ReportGenerationException(String message, Throwable cause, String errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
}

