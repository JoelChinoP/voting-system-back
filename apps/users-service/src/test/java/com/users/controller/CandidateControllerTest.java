package com.users.controller;

import com.users.dto.CandidateRequest;
import com.users.dto.CandidateResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

@SpringBootTest
class CandidateControllerTest {

    private CandidateRequest candidateRequest;
    private CandidateResponse candidateResponse;

    @BeforeEach
    void setUp() {
        candidateRequest = new CandidateRequest();
        candidateRequest.setNombre("Juan Pérez");
        candidateRequest.setPartidoPolitico("Partido Liberal");
        candidateRequest.setPropuestas("Propuestas de mejora educativa");

        candidateResponse = new CandidateResponse();
        candidateResponse.setId(1L);
        candidateResponse.setNombre("Juan Pérez");
        candidateResponse.setPartidoPolitico("Partido Liberal");
        candidateResponse.setPropuestas("Propuestas de mejora educativa");
        candidateResponse.setCreatedAt(LocalDateTime.now());
        candidateResponse.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testSetUp() {
        // Test básico para verificar que el setup funciona correctamente
        assert candidateRequest.getNombre().equals("Juan Pérez");
        assert candidateResponse.getId().equals(1L);
    }
}
