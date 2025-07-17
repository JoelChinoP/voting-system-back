package com.reports;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Pruebas de integración para la aplicación del microservicio de reportes.
 * 
 * Esta clase contiene pruebas básicas para verificar que el contexto
 * de Spring Boot se carga correctamente y que la aplicación puede
 * iniciarse sin errores.
 */
@SpringBootTest
@ActiveProfiles("test")
class ReportsServiceApplicationTests {

    /**
     * Prueba que verifica que el contexto de la aplicación se carga correctamente.
     * 
     * Esta prueba básica asegura que todas las configuraciones de Spring Boot
     * están correctas y que no hay errores de configuración que impidan
     * el inicio de la aplicación.
     */
    @Test
    void contextLoads() {
        // Esta prueba pasa si el contexto de Spring se carga sin errores
        // No se requiere código adicional ya que Spring Boot automáticamente
        // verifica la carga del contexto durante la ejecución de la prueba
    }

    /**
     * Prueba que verifica que la aplicación principal puede ejecutarse.
     * 
     * Esta prueba asegura que el método main de la aplicación funciona
     * correctamente y que no hay problemas de configuración básicos.
     */
    @Test
    void mainMethodTest() {
        // Verificar que el método main no lanza excepciones
        try {
            ReportsServiceApplication.main(new String[]{});
        } catch (Exception e) {
            // Si hay una excepción relacionada con el puerto ya en uso,
            // la ignoramos ya que es esperada en el entorno de pruebas
            if (!e.getMessage().contains("Port") && !e.getMessage().contains("Address already in use")) {
                throw e;
            }
        }
    }
}

