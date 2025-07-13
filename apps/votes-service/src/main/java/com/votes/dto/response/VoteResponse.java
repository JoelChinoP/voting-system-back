package com.votes.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoteResponse {

    private UUID voteId;
    private UUID candidateId;
    private UUID electionId;
    private LocalDateTime votedAt;
    private String message;
    private boolean success;

    public VoteResponse(UUID voteId, UUID candidateId, UUID electionId, LocalDateTime votedAt, boolean success) {
        this.voteId = voteId;
        this.candidateId = candidateId;
        this.electionId = electionId;
        this.votedAt = votedAt;
        this.success = success;
        this.message = success ? "Voto registrado exitosamente" : "Error al registrar el voto";
    }

    public static VoteResponse success(UUID voteId, UUID candidateId, UUID electionId, LocalDateTime votedAt) {
        return new VoteResponse(voteId, candidateId, electionId, votedAt, true);
    }

    public static VoteResponse error(String message) {
        VoteResponse response = new VoteResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }
}
