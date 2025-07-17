package com.reports;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Aplicación principal del microservicio de reportes.
 * 
 * Este microservicio se encarga de generar y proporcionar reportes
 * relacionados con los resultados de las votaciones en el sistema
 * de votación electrónica distribuida.
 * 
 * @author Sistema de Votación Electrónica
 * @version 1.0
 */
@SpringBootApplication
public class ReportsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReportsServiceApplication.class, args);
    }

}

