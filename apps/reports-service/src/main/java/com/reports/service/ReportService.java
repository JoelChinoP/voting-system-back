package com.reports.service;

import com.reports.dto.CandidateVotesDTO;
import com.reports.dto.OverallResultsDTO;
import com.reports.exception.ReportGenerationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio principal para la generación de reportes.
 * 
 * Esta clase contiene la lógica de negocio para generar diferentes tipos
 * de reportes relacionados con los resultados de las votaciones, incluyendo
 * la comunicación con otros microservicios para obtener los datos necesarios.
 */
@Service
public class ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    @Value("${votes.service.url:http://localhost:8083}")
    private String votesServiceUrl;

    @Value("${users.service.url:http://localhost:8082}")
    private String usersServiceUrl;

    private final RestTemplate restTemplate;

    public ReportService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Obtiene los votos por candidato desde el servicio de votos.
     * 
     * @return Lista de votos por candidato
     * @throws ReportGenerationException Si hay error al obtener los datos
     */
    public List<CandidateVotesDTO> getVotesByCandidate() {
        try {
            logger.info("Obteniendo votos por candidato desde el servicio de votos");
            
            // Llamada al endpoint del votes-service para obtener los conteos
            String url = votesServiceUrl + "/api/v1/votes/results/by-candidate";
            
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );

            if (response.getBody() == null) {
                throw new ReportGenerationException("No se recibieron datos del servicio de votos", "VOTES_SERVICE_NO_DATA");
            }

            // Convertir la respuesta a DTOs
            List<CandidateVotesDTO> candidateVotes = response.getBody().stream()
                .map(this::mapToCandidateVotesDTO)
                .collect(Collectors.toList());

            // Calcular porcentajes
            long totalVotes = candidateVotes.stream()
                .mapToLong(CandidateVotesDTO::getVoteCount)
                .sum();

            candidateVotes.forEach(candidate -> {
                if (totalVotes > 0) {
                    double percentage = (candidate.getVoteCount() * 100.0) / totalVotes;
                    candidate.setPercentage(Math.round(percentage * 100.0) / 100.0);
                } else {
                    candidate.setPercentage(0.0);
                }
            });

            logger.info("Se obtuvieron {} candidatos con votos", candidateVotes.size());
            return candidateVotes;

        } catch (RestClientException e) {
            logger.error("Error al comunicarse con el servicio de votos: {}", e.getMessage(), e);
            throw new ReportGenerationException("Error al obtener datos del servicio de votos", e, "VOTES_SERVICE_ERROR");
        } catch (Exception e) {
            logger.error("Error inesperado al obtener votos por candidato: {}", e.getMessage(), e);
            throw new ReportGenerationException("Error inesperado al generar el reporte de votos", e, "UNEXPECTED_ERROR");
        }
    }

    /**
     * Genera un reporte general de resultados de la votación.
     * 
     * @return Resultados generales de la votación
     * @throws ReportGenerationException Si hay error al generar el reporte
     */
    public OverallResultsDTO getOverallResults() {
        try {
            logger.info("Generando reporte general de resultados");

            // Obtener votos por candidato
            List<CandidateVotesDTO> candidateResults = getVotesByCandidate();

            // Calcular totales
            long totalVotes = candidateResults.stream()
                .mapToLong(CandidateVotesDTO::getVoteCount)
                .sum();

            // Encontrar el candidato ganador
            CandidateVotesDTO winner = candidateResults.stream()
                .max((c1, c2) -> Long.compare(c1.getVoteCount(), c2.getVoteCount()))
                .orElse(null);

            // Obtener número total de usuarios registrados (opcional)
            Long totalRegisteredUsers = getTotalRegisteredUsers();

            // Crear el DTO de resultados generales
            OverallResultsDTO results = new OverallResultsDTO();
            results.setTotalVotesCast(totalVotes);
            results.setCandidateResults(candidateResults);
            results.setVotingStatus("ACTIVE"); // Por defecto, se puede obtener de otro servicio

            if (winner != null) {
                results.setWinningCandidateId(winner.getCandidateId());
                results.setWinningCandidateName(winner.getCandidateName());
            }

            if (totalRegisteredUsers != null && totalRegisteredUsers > 0) {
                results.setTotalRegisteredUsers(totalRegisteredUsers);
                double participation = (totalVotes * 100.0) / totalRegisteredUsers;
                results.setParticipationPercentage(Math.round(participation * 100.0) / 100.0);
            }

            logger.info("Reporte general generado exitosamente. Total de votos: {}, Ganador: {}", 
                       totalVotes, winner != null ? winner.getCandidateName() : "N/A");

            return results;

        } catch (ReportGenerationException e) {
            // Re-lanzar excepciones de generación de reportes
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado al generar reporte general: {}", e.getMessage(), e);
            throw new ReportGenerationException("Error inesperado al generar el reporte general", e, "UNEXPECTED_ERROR");
        }
    }

    /**
     * Obtiene el número total de usuarios registrados desde el servicio de usuarios.
     * 
     * @return Número total de usuarios registrados, o null si no se puede obtener
     */
    private Long getTotalRegisteredUsers() {
        try {
            logger.debug("Obteniendo número total de usuarios registrados");
            
            String url = usersServiceUrl + "/api/v1/users/count";
            
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (response.getBody() != null && response.getBody().containsKey("count")) {
                Object countObj = response.getBody().get("count");
                if (countObj instanceof Number) {
                    return ((Number) countObj).longValue();
                }
            }

            return null;

        } catch (Exception e) {
            logger.warn("No se pudo obtener el número total de usuarios: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Convierte un mapa de datos a un DTO de votos por candidato.
     * 
     * @param data Mapa con los datos del candidato
     * @return DTO de votos por candidato
     */
    private CandidateVotesDTO mapToCandidateVotesDTO(Map<String, Object> data) {
        String candidateId = data.get("candidateId") != null ? data.get("candidateId").toString() : "";
        String candidateName = data.get("candidateName") != null ? data.get("candidateName").toString() : "Candidato Desconocido";
        
        Long voteCount = 0L;
        Object voteCountObj = data.get("voteCount");
        if (voteCountObj instanceof Number) {
            voteCount = ((Number) voteCountObj).longValue();
        }

        return new CandidateVotesDTO(candidateId, candidateName, voteCount);
    }
}

