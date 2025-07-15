package com.users.service;

import com.users.dto.CandidateRequest;
import com.users.dto.CandidateResponse;
import com.users.entity.Candidate;
import com.users.exception.CandidateAlreadyExistsException;
import com.users.exception.CandidateNotFoundException;
import com.users.repository.CandidateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests del Servicio de Candidatos")
class CandidateServiceTest {

    @Mock
    private CandidateRepository candidateRepository;

    @InjectMocks
    private CandidateService candidateService;

    private CandidateRequest candidateRequest;
    private Candidate candidate;
    private CandidateResponse expectedResponse;

    @BeforeEach
    void setUp() {
        candidateRequest = new CandidateRequest();
        candidateRequest.setNombre("Juan Pérez");
        candidateRequest.setPartidoPolitico("Partido Liberal");
        candidateRequest.setPropuestas("Propuestas de mejora educativa");

        candidate = new Candidate();
        candidate.setId(1L);
        candidate.setNombre("Juan Pérez");
        candidate.setPartidoPolitico("Partido Liberal");
        candidate.setPropuestas("Propuestas de mejora educativa");
        candidate.setCreatedAt(LocalDateTime.now());
        candidate.setUpdatedAt(LocalDateTime.now());

        expectedResponse = new CandidateResponse();
        expectedResponse.setId(1L);
        expectedResponse.setNombre("Juan Pérez");
        expectedResponse.setPartidoPolitico("Partido Liberal");
        expectedResponse.setPropuestas("Propuestas de mejora educativa");
        expectedResponse.setCreatedAt(candidate.getCreatedAt());
        expectedResponse.setUpdatedAt(candidate.getUpdatedAt());
    }

    @Test
    @DisplayName("Debe crear candidato exitosamente cuando no existe")
    void shouldCreateCandidateSuccessfully() {
        // Given
        when(candidateRepository.existsByNombreIgnoreCaseAndPartidoPoliticoIgnoreCase(
                anyString(), anyString())).thenReturn(false);
        when(candidateRepository.save(any(Candidate.class))).thenReturn(candidate);

        // When
        CandidateResponse result = candidateService.createCandidate(candidateRequest);

        // Then
        assertNotNull(result);
        assertEquals(expectedResponse.getNombre(), result.getNombre());
        assertEquals(expectedResponse.getPartidoPolitico(), result.getPartidoPolitico());
        assertEquals(expectedResponse.getPropuestas(), result.getPropuestas());
        verify(candidateRepository).existsByNombreIgnoreCaseAndPartidoPoliticoIgnoreCase(
                candidateRequest.getNombre(), candidateRequest.getPartidoPolitico());
        verify(candidateRepository).save(any(Candidate.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando candidato ya existe")
    void shouldThrowExceptionWhenCandidateAlreadyExists() {
        // Given
        when(candidateRepository.existsByNombreIgnoreCaseAndPartidoPoliticoIgnoreCase(
                anyString(), anyString())).thenReturn(true);

        // When & Then
        CandidateAlreadyExistsException exception = assertThrows(
                CandidateAlreadyExistsException.class,
                () -> candidateService.createCandidate(candidateRequest)
        );

        assertTrue(exception.getMessage().contains("Ya existe un candidato"));
        verify(candidateRepository).existsByNombreIgnoreCaseAndPartidoPoliticoIgnoreCase(
                candidateRequest.getNombre(), candidateRequest.getPartidoPolitico());
        verify(candidateRepository, never()).save(any(Candidate.class));
    }

    @Test
    @DisplayName("Debe obtener todos los candidatos exitosamente")
    void shouldGetAllCandidatesSuccessfully() {
        // Given
        List<Candidate> candidates = Arrays.asList(candidate);
        when(candidateRepository.findAllOrderByNombre()).thenReturn(candidates);

        // When
        List<CandidateResponse> result = candidateService.getAllCandidates();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedResponse.getNombre(), result.get(0).getNombre());
        verify(candidateRepository).findAllOrderByNombre();
    }

    @Test
    @DisplayName("Debe obtener candidato por ID exitosamente")
    void shouldGetCandidateByIdSuccessfully() {
        // Given
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(candidate));

        // When
        CandidateResponse result = candidateService.getCandidateById(1L);

        // Then
        assertNotNull(result);
        assertEquals(expectedResponse.getId(), result.getId());
        assertEquals(expectedResponse.getNombre(), result.getNombre());
        verify(candidateRepository).findById(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando candidato no existe por ID")
    void shouldThrowExceptionWhenCandidateNotFoundById() {
        // Given
        when(candidateRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        CandidateNotFoundException exception = assertThrows(
                CandidateNotFoundException.class,
                () -> candidateService.getCandidateById(1L)
        );

        assertTrue(exception.getMessage().contains("Candidato no encontrado"));
        verify(candidateRepository).findById(1L);
    }

    @Test
    @DisplayName("Debe buscar candidatos por nombre exitosamente")
    void shouldSearchCandidatesByNameSuccessfully() {
        // Given
        List<Candidate> candidates = Arrays.asList(candidate);
        when(candidateRepository.findByNombreContainingIgnoreCase("Juan")).thenReturn(candidates);

        // When
        List<CandidateResponse> result = candidateService.searchCandidatesByName("Juan");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedResponse.getNombre(), result.get(0).getNombre());
        verify(candidateRepository).findByNombreContainingIgnoreCase("Juan");
    }

    @Test
    @DisplayName("Debe obtener candidatos por partido exitosamente")
    void shouldGetCandidatesByPartidoSuccessfully() {
        // Given
        List<Candidate> candidates = Arrays.asList(candidate);
        when(candidateRepository.findByPartidoPoliticoIgnoreCase("Partido Liberal")).thenReturn(candidates);

        // When
        List<CandidateResponse> result = candidateService.getCandidatesByPartido("Partido Liberal");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedResponse.getPartidoPolitico(), result.get(0).getPartidoPolitico());
        verify(candidateRepository).findByPartidoPoliticoIgnoreCase("Partido Liberal");
    }

    @Test
    @DisplayName("Debe eliminar candidato exitosamente")
    void shouldDeleteCandidateSuccessfully() {
        // Given
        when(candidateRepository.existsById(1L)).thenReturn(true);

        // When
        candidateService.deleteCandidate(1L);

        // Then
        verify(candidateRepository).existsById(1L);
        verify(candidateRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción al eliminar candidato inexistente")
    void shouldThrowExceptionWhenDeletingNonExistentCandidate() {
        // Given
        when(candidateRepository.existsById(1L)).thenReturn(false);

        // When & Then
        CandidateNotFoundException exception = assertThrows(
                CandidateNotFoundException.class,
                () -> candidateService.deleteCandidate(1L)
        );

        assertTrue(exception.getMessage().contains("Candidato no encontrado"));
        verify(candidateRepository).existsById(1L);
        verify(candidateRepository, never()).deleteById(1L);
    }
}
