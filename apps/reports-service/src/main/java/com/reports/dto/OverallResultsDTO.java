package com.reports.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para representar los resultados generales de la votación.
 * 
 * Esta clase encapsula un resumen completo de los resultados electorales,
 * incluyendo información del candidato ganador, participación total y
 * estadísticas generales.
 */
@Schema(description = "Resultados generales de la votación")
public class OverallResultsDTO {

    @Schema(description = "ID del candidato ganador", example = "1")
    @JsonProperty("winningCandidateId")
    private String winningCandidateId;

    @Schema(description = "Nombre del candidato ganador", example = "Juan Pérez")
    @JsonProperty("winningCandidateName")
    private String winningCandidateName;

    @Schema(description = "Número total de votos emitidos", example = "500")
    @JsonProperty("totalVotesCast")
    private Long totalVotesCast;

    @Schema(description = "Número total de usuarios registrados", example = "750")
    @JsonProperty("totalRegisteredUsers")
    private Long totalRegisteredUsers;

    @Schema(description = "Porcentaje de participación", example = "66.67")
    @JsonProperty("participationPercentage")
    private Double participationPercentage;

    @Schema(description = "Lista detallada de votos por candidato")
    @JsonProperty("candidateResults")
    private List<CandidateVotesDTO> candidateResults;

    @Schema(description = "Fecha y hora de generación del reporte")
    @JsonProperty("reportGeneratedAt")
    private LocalDateTime reportGeneratedAt;

    @Schema(description = "Estado de la votación", example = "ACTIVE", allowableValues = {"ACTIVE", "CLOSED", "PENDING"})
    @JsonProperty("votingStatus")
    private String votingStatus;

    /**
     * Constructor por defecto.
     */
    public OverallResultsDTO() {
        this.reportGeneratedAt = LocalDateTime.now();
    }

    /**
     * Constructor con parámetros básicos.
     * 
     * @param winningCandidateId ID del candidato ganador
     * @param winningCandidateName Nombre del candidato ganador
     * @param totalVotesCast Total de votos emitidos
     * @param candidateResults Lista de resultados por candidato
     */
    public OverallResultsDTO(String winningCandidateId, String winningCandidateName, 
                           Long totalVotesCast, List<CandidateVotesDTO> candidateResults) {
        this.winningCandidateId = winningCandidateId;
        this.winningCandidateName = winningCandidateName;
        this.totalVotesCast = totalVotesCast;
        this.candidateResults = candidateResults;
        this.reportGeneratedAt = LocalDateTime.now();
    }

    // Getters y Setters

    public String getWinningCandidateId() {
        return winningCandidateId;
    }

    public void setWinningCandidateId(String winningCandidateId) {
        this.winningCandidateId = winningCandidateId;
    }

    public String getWinningCandidateName() {
        return winningCandidateName;
    }

    public void setWinningCandidateName(String winningCandidateName) {
        this.winningCandidateName = winningCandidateName;
    }

    public Long getTotalVotesCast() {
        return totalVotesCast;
    }

    public void setTotalVotesCast(Long totalVotesCast) {
        this.totalVotesCast = totalVotesCast;
    }

    public Long getTotalRegisteredUsers() {
        return totalRegisteredUsers;
    }

    public void setTotalRegisteredUsers(Long totalRegisteredUsers) {
        this.totalRegisteredUsers = totalRegisteredUsers;
    }

    public Double getParticipationPercentage() {
        return participationPercentage;
    }

    public void setParticipationPercentage(Double participationPercentage) {
        this.participationPercentage = participationPercentage;
    }

    public List<CandidateVotesDTO> getCandidateResults() {
        return candidateResults;
    }

    public void setCandidateResults(List<CandidateVotesDTO> candidateResults) {
        this.candidateResults = candidateResults;
    }

    public LocalDateTime getReportGeneratedAt() {
        return reportGeneratedAt;
    }

    public void setReportGeneratedAt(LocalDateTime reportGeneratedAt) {
        this.reportGeneratedAt = reportGeneratedAt;
    }

    public String getVotingStatus() {
        return votingStatus;
    }

    public void setVotingStatus(String votingStatus) {
        this.votingStatus = votingStatus;
    }

    @Override
    public String toString() {
        return "OverallResultsDTO{" +
                "winningCandidateId='" + winningCandidateId + '\'' +
                ", winningCandidateName='" + winningCandidateName + '\'' +
                ", totalVotesCast=" + totalVotesCast +
                ", totalRegisteredUsers=" + totalRegisteredUsers +
                ", participationPercentage=" + participationPercentage +
                ", candidateResults=" + candidateResults +
                ", reportGeneratedAt=" + reportGeneratedAt +
                ", votingStatus='" + votingStatus + '\'' +
                '}';
    }
}

