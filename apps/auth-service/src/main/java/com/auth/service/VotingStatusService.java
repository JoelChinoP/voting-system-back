package com.auth.service;

import com.auth.entity.cassandra.UserVoteLog;
import com.auth.repository.cassandra.UserVoteLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
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
     * Consulta el repositorio de votos en Cassandra para determinar si el usuario
     * ya votó
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
     * RF-005: Marcar usuario como votado
     */
    public void markUserVoted(UUID userId, UUID electionId) {
        try {
            if (electionId == null) {
                electionId = UUID.fromString(defaultElectionId);
            }

            // Crear el log con identificador único y sin candidato específico
            UserVoteLog logEntry = UserVoteLog.builder()
                    .userId(userId)
                    .electionId(electionId)
                    .voteId(UUID.randomUUID()) // ID único del voto (generado internamente)
                    .candidateId(null) // Si no se conoce el candidato aquí
                    .votedAt(Instant.now())
                    .build();

            userVoteLogRepository.save(logEntry);
            this.log.info("Persistido voto para usuario {} en elección {}", userId, electionId);

        } catch (Exception e) {
            this.log.error("Error al persistir voto para usuario {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("No se pudo registrar el voto", e);
        }
    }

    /**
     * Verificación con elección por defecto
     */
    public boolean hasUserVoted(UUID userId) {
        return hasUserVoted(userId, UUID.fromString(defaultElectionId));
    }
}
