package com.votes.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoteRequest {

    @NotNull(message = "El ID del candidato es requerido")
    private UUID candidateId;

    // Optional - se usará el election_id por defecto si no se proporciona
    private UUID electionId;
}
