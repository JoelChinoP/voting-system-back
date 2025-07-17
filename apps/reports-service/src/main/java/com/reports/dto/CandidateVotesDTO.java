package com.reports.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO para representar los votos de un candidato.
 * 
 * Esta clase encapsula la información sobre el número de votos
 * recibidos por un candidato específico en la votación.
 */
@Schema(description = "Información de votos por candidato")
public class CandidateVotesDTO {

    @Schema(description = "ID único del candidato", example = "1")
    @JsonProperty("candidateId")
    private String candidateId;

    @Schema(description = "Nombre completo del candidato", example = "Juan Pérez")
    @JsonProperty("candidateName")
    private String candidateName;

    @Schema(description = "Número total de votos recibidos", example = "150")
    @JsonProperty("voteCount")
    private Long voteCount;

    @Schema(description = "Porcentaje de votos respecto al total", example = "25.5")
    @JsonProperty("percentage")
    private Double percentage;

    /**
     * Constructor por defecto.
     */
    public CandidateVotesDTO() {
    }

    /**
     * Constructor con parámetros.
     * 
     * @param candidateId ID del candidato
     * @param candidateName Nombre del candidato
     * @param voteCount Número de votos
     */
    public CandidateVotesDTO(String candidateId, String candidateName, Long voteCount) {
        this.candidateId = candidateId;
        this.candidateName = candidateName;
        this.voteCount = voteCount;
    }

    /**
     * Constructor completo con porcentaje.
     * 
     * @param candidateId ID del candidato
     * @param candidateName Nombre del candidato
     * @param voteCount Número de votos
     * @param percentage Porcentaje de votos
     */
    public CandidateVotesDTO(String candidateId, String candidateName, Long voteCount, Double percentage) {
        this.candidateId = candidateId;
        this.candidateName = candidateName;
        this.voteCount = voteCount;
        this.percentage = percentage;
    }

    // Getters y Setters

    public String getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(String candidateId) {
        this.candidateId = candidateId;
    }

    public String getCandidateName() {
        return candidateName;
    }

    public void setCandidateName(String candidateName) {
        this.candidateName = candidateName;
    }

    public Long getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(Long voteCount) {
        this.voteCount = voteCount;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }

    @Override
    public String toString() {
        return "CandidateVotesDTO{" +
                "candidateId='" + candidateId + '\'' +
                ", candidateName='" + candidateName + '\'' +
                ", voteCount=" + voteCount +
                ", percentage=" + percentage +
                '}';
    }
}

