package com.auth.service;

import com.auth.repository.cassandra.UserVoteLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VotingStatusService {
    
    private final UserVoteLogRepository userVoteLogRepository;
    
    @Value("${app.default-election-id:550e8400-e29b-41d4-a716-446655440000}")
    private String defaultElectionId;
    
    /**
     * RF-003: Verificación de elegibilidad para votar
     * Consulta el repositorio de votos en Cassandra para determinar si el usuario ya votó
     */
    public boolean hasUserVoted(UUID userId, UUID electionId) {
        try {
            if (electionId == null) {
                electionId = UUID.fromString(defaultElectionId);
            }
            
            // Verificar en Cassandra si existe un log de voto para este usuario
            boolean hasVoted = userVoteLogRepository.existsByUserIdAndElectionId(userId, electionId);
            
            log.info("Usuario {} {} ha votado en la eleccion {}", 
                userId, hasVoted ? "SI" : "NO", electionId);
            
            return hasVoted;
            
        } catch (Exception e) {
            log.error("Error verificando el estado de votación para usuario {}: {}", 
                userId, e.getMessage(), e);
            
            // En caso de error, consideramos que no ha votado (fail-safe)
            return false;
        }
    }
    
    /**
     * Verificación con elección por defecto
     */
    public boolean hasUserVoted(UUID userId) {
        return hasUserVoted(userId, UUID.fromString(defaultElectionId));
    }
}
