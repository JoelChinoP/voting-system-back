package com.reports.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reports.dto.CandidateVotesDTO;
import com.reports.dto.OverallResultsDTO;
import com.reports.exception.ReportGenerationException;
import com.reports.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas unitarias para el controlador de reportes.
 * 
 * Esta clase contiene pruebas para verificar el correcto funcionamiento
 * de los endpoints REST del controlador de reportes, incluyendo la
 * validación de respuestas y el manejo de errores.
 */
@WebMvcTest(ReportController.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @Autowired
    private ObjectMapper objectMapper;

    private List<CandidateVotesDTO> mockCandidateVotes;
    private OverallResultsDTO mockOverallResults;

    @BeforeEach
    void setUp() {
        // Preparar datos de prueba
        mockCandidateVotes = Arrays.asList(
            new CandidateVotesDTO("1", "Juan Pérez", 150L, 60.0),
            new CandidateVotesDTO("2", "María García", 100L, 40.0)
        );

        mockOverallResults = new OverallResultsDTO();
        mockOverallResults.setWinningCandidateId("1");
        mockOverallResults.setWinningCandidateName("Juan Pérez");
        mockOverallResults.setTotalVotesCast(250L);
        mockOverallResults.setTotalRegisteredUsers(500L);
        mockOverallResults.setParticipationPercentage(50.0);
        mockOverallResults.setCandidateResults(mockCandidateVotes);
        mockOverallResults.setVotingStatus("ACTIVE");
        mockOverallResults.setReportGeneratedAt(LocalDateTime.now());
    }

    /**
     * Prueba exitosa para obtener votos por candidato.
     */
    @Test
    @WithMockUser(username = "testuser")
    void getVotesByCandidate_Success() throws Exception {
        // Configurar mock
        when(reportService.getVotesByCandidate()).thenReturn(mockCandidateVotes);

        // Ejecutar y verificar
        mockMvc.perform(get("/api/v1/reports/votes-by-candidate")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Reporte de votos por candidato generado exitosamente"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].candidateId").value("1"))
                .andExpect(jsonPath("$.data[0].candidateName").value("Juan Pérez"))
                .andExpect(jsonPath("$.data[0].voteCount").value(150))
                .andExpect(jsonPath("$.data[0].percentage").value(60.0))
                .andExpect(jsonPath("$.timestamp").exists());

        // Verificar que se llamó al servicio
        verify(reportService, times(1)).getVotesByCandidate();
    }

    /**
     * Prueba para manejar error en el servicio al obtener votos por candidato.
     */
    @Test
    @WithMockUser(username = "testuser")
    void getVotesByCandidate_ServiceError() throws Exception {
        // Configurar mock para lanzar excepción
        when(reportService.getVotesByCandidate())
            .thenThrow(new ReportGenerationException("Error al obtener datos", "VOTES_SERVICE_ERROR"));

        // Ejecutar y verificar
        mockMvc.perform(get("/api/v1/reports/votes-by-candidate")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpected(jsonPath("$.message").value("Error al obtener datos"))
                .andExpect(jsonPath("$.errorCode").value("VOTES_SERVICE_ERROR"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(reportService, times(1)).getVotesByCandidate();
    }

    /**
     * Prueba para acceso no autorizado a votos por candidato.
     */
    @Test
    void getVotesByCandidate_Unauthorized() throws Exception {
        // Ejecutar sin autenticación
        mockMvc.perform(get("/api/v1/reports/votes-by-candidate")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        // Verificar que no se llamó al servicio
        verify(reportService, never()).getVotesByCandidate();
    }

    /**
     * Prueba exitosa para obtener resultados generales.
     */
    @Test
    @WithMockUser(username = "testuser")
    void getOverallResults_Success() throws Exception {
        // Configurar mock
        when(reportService.getOverallResults()).thenReturn(mockOverallResults);

        // Ejecutar y verificar
        mockMvc.perform(get("/api/v1/reports/overall-results")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Reporte de resultados generales generado exitosamente"))
                .andExpect(jsonPath("$.data.winningCandidateId").value("1"))
                .andExpect(jsonPath("$.data.winningCandidateName").value("Juan Pérez"))
                .andExpect(jsonPath("$.data.totalVotesCast").value(250))
                .andExpect(jsonPath("$.data.totalRegisteredUsers").value(500))
                .andExpect(jsonPath("$.data.participationPercentage").value(50.0))
                .andExpect(jsonPath("$.data.votingStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.data.candidateResults").isArray())
                .andExpect(jsonPath("$.data.candidateResults.length()").value(2))
                .andExpect(jsonPath("$.data.reportGeneratedAt").exists())
                .andExpect(jsonPath("$.timestamp").exists());

        verify(reportService, times(1)).getOverallResults();
    }

    /**
     * Prueba para manejar error en el servicio al obtener resultados generales.
     */
    @Test
    @WithMockUser(username = "testuser")
    void getOverallResults_ServiceError() throws Exception {
        // Configurar mock para lanzar excepción
        when(reportService.getOverallResults())
            .thenThrow(new ReportGenerationException("Error al generar reporte", "REPORT_GENERATION_ERROR"));

        // Ejecutar y verificar
        mockMvc.perform(get("/api/v1/reports/overall-results")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Error al generar reporte"))
                .andExpect(jsonPath("$.errorCode").value("REPORT_GENERATION_ERROR"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(reportService, times(1)).getOverallResults();
    }

    /**
     * Prueba exitosa para health check.
     */
    @Test
    @WithMockUser(username = "testuser")
    void healthCheck_Success() throws Exception {
        // Ejecutar y verificar
        mockMvc.perform(get("/api/v1/reports/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Servicio de reportes funcionando correctamente"))
                .andExpect(jsonPath("$.data").value("OK"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    /**
     * Prueba exitosa para refrescar reportes.
     */
    @Test
    @WithMockUser(username = "testuser")
    void refreshReports_Success() throws Exception {
        // Configurar mock
        when(reportService.getOverallResults()).thenReturn(mockOverallResults);

        // Ejecutar y verificar
        mockMvc.perform(post("/api/v1/reports/refresh")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Datos de reportes actualizados exitosamente"))
                .andExpect(jsonPath("$.data").value("Actualizado"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(reportService, times(1)).getOverallResults();
    }

    /**
     * Prueba para manejar error al refrescar reportes.
     */
    @Test
    @WithMockUser(username = "testuser")
    void refreshReports_ServiceError() throws Exception {
        // Configurar mock para lanzar excepción
        when(reportService.getOverallResults())
            .thenThrow(new ReportGenerationException("Error al actualizar", "UPDATE_ERROR"));

        // Ejecutar y verificar
        mockMvc.perform(post("/api/v1/reports/refresh")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Error al actualizar"))
                .andExpect(jsonPath("$.errorCode").value("UPDATE_ERROR"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(reportService, times(1)).getOverallResults();
    }
}

