package com.reports.service;

import com.reports.dto.CandidateVotesDTO;
import com.reports.dto.OverallResultsDTO;
import com.reports.exception.ReportGenerationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para el servicio de reportes.
 * 
 * Esta clase contiene pruebas para verificar el correcto funcionamiento
 * de la lógica de negocio del servicio de reportes, incluyendo la
 * comunicación con servicios externos y el manejo de errores.
 */
@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ReportService reportService;

    @BeforeEach
    void setUp() {
        // Configurar las URLs de los servicios para las pruebas
        ReflectionTestUtils.setField(reportService, "votesServiceUrl", "http://localhost:8083");
        ReflectionTestUtils.setField(reportService, "usersServiceUrl", "http://localhost:8082");
        ReflectionTestUtils.setField(reportService, "restTemplate", restTemplate);
    }

    /**
     * Prueba exitosa para obtener votos por candidato.
     */
    @Test
    void getVotesByCandidate_Success() {
        // Preparar datos de prueba
        List<Map<String, Object>> mockResponse = Arrays.asList(
            createCandidateMap("1", "Juan Pérez", 150L),
            createCandidateMap("2", "María García", 100L),
            createCandidateMap("3", "Carlos López", 75L)
        );

        ResponseEntity<List<Map<String, Object>>> responseEntity = 
            new ResponseEntity<>(mockResponse, HttpStatus.OK);

        // Configurar mock
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            isNull(),
            any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        // Ejecutar método
        List<CandidateVotesDTO> result = reportService.getVotesByCandidate();

        // Verificar resultados
        assertNotNull(result);
        assertEquals(3, result.size());
        
        CandidateVotesDTO firstCandidate = result.get(0);
        assertEquals("1", firstCandidate.getCandidateId());
        assertEquals("Juan Pérez", firstCandidate.getCandidateName());
        assertEquals(150L, firstCandidate.getVoteCount());
        assertEquals(46.15, firstCandidate.getPercentage(), 0.01); // 150/325 * 100

        // Verificar que se llamó al servicio correcto
        verify(restTemplate, times(1)).exchange(
            eq("http://localhost:8083/api/v1/votes/results/by-candidate"),
            eq(HttpMethod.GET),
            isNull(),
            any(ParameterizedTypeReference.class)
        );
    }

    /**
     * Prueba para manejar respuesta vacía del servicio de votos.
     */
    @Test
    void getVotesByCandidate_EmptyResponse() {
        // Configurar mock para respuesta vacía
        ResponseEntity<List<Map<String, Object>>> responseEntity = 
            new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            isNull(),
            any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        // Ejecutar y verificar excepción
        ReportGenerationException exception = assertThrows(
            ReportGenerationException.class,
            () -> reportService.getVotesByCandidate()
        );

        assertEquals("VOTES_SERVICE_NO_DATA", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("No se recibieron datos del servicio de votos"));
    }

    /**
     * Prueba para manejar error de comunicación con el servicio de votos.
     */
    @Test
    void getVotesByCandidate_RestClientException() {
        // Configurar mock para lanzar excepción
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            isNull(),
            any(ParameterizedTypeReference.class)
        )).thenThrow(new RestClientException("Connection refused"));

        // Ejecutar y verificar excepción
        ReportGenerationException exception = assertThrows(
            ReportGenerationException.class,
            () -> reportService.getVotesByCandidate()
        );

        assertEquals("VOTES_SERVICE_ERROR", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Error al obtener datos del servicio de votos"));
    }

    /**
     * Prueba exitosa para obtener resultados generales.
     */
    @Test
    void getOverallResults_Success() {
        // Preparar datos de prueba para votos
        List<Map<String, Object>> mockVotesResponse = Arrays.asList(
            createCandidateMap("1", "Juan Pérez", 150L),
            createCandidateMap("2", "María García", 100L)
        );

        ResponseEntity<List<Map<String, Object>>> votesResponseEntity = 
            new ResponseEntity<>(mockVotesResponse, HttpStatus.OK);

        // Preparar datos de prueba para usuarios
        Map<String, Object> mockUsersResponse = new HashMap<>();
        mockUsersResponse.put("count", 500L);

        ResponseEntity<Map<String, Object>> usersResponseEntity = 
            new ResponseEntity<>(mockUsersResponse, HttpStatus.OK);

        // Configurar mocks
        when(restTemplate.exchange(
            eq("http://localhost:8083/api/v1/votes/results/by-candidate"),
            eq(HttpMethod.GET),
            isNull(),
            any(ParameterizedTypeReference.class)
        )).thenReturn(votesResponseEntity);

        when(restTemplate.exchange(
            eq("http://localhost:8082/api/v1/users/count"),
            eq(HttpMethod.GET),
            isNull(),
            any(ParameterizedTypeReference.class)
        )).thenReturn(usersResponseEntity);

        // Ejecutar método
        OverallResultsDTO result = reportService.getOverallResults();

        // Verificar resultados
        assertNotNull(result);
        assertEquals(250L, result.getTotalVotesCast()); // 150 + 100
        assertEquals(500L, result.getTotalRegisteredUsers());
        assertEquals(50.0, result.getParticipationPercentage(), 0.01); // 250/500 * 100
        assertEquals("1", result.getWinningCandidateId());
        assertEquals("Juan Pérez", result.getWinningCandidateName());
        assertEquals("ACTIVE", result.getVotingStatus());
        assertNotNull(result.getCandidateResults());
        assertEquals(2, result.getCandidateResults().size());
        assertNotNull(result.getReportGeneratedAt());
    }

    /**
     * Prueba para resultados generales cuando no hay votos.
     */
    @Test
    void getOverallResults_NoVotes() {
        // Preparar respuesta vacía
        ResponseEntity<List<Map<String, Object>>> responseEntity = 
            new ResponseEntity<>(Arrays.asList(), HttpStatus.OK);

        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            isNull(),
            any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        // Ejecutar método
        OverallResultsDTO result = reportService.getOverallResults();

        // Verificar resultados
        assertNotNull(result);
        assertEquals(0L, result.getTotalVotesCast());
        assertNull(result.getWinningCandidateId());
        assertNull(result.getWinningCandidateName());
        assertEquals("ACTIVE", result.getVotingStatus());
        assertNotNull(result.getCandidateResults());
        assertTrue(result.getCandidateResults().isEmpty());
    }

    /**
     * Método auxiliar para crear mapas de candidatos para las pruebas.
     */
    private Map<String, Object> createCandidateMap(String id, String name, Long votes) {
        Map<String, Object> candidate = new HashMap<>();
        candidate.put("candidateId", id);
        candidate.put("candidateName", name);
        candidate.put("voteCount", votes);
        return candidate;
    }
}

