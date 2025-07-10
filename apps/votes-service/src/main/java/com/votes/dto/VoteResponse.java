package com.votes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoteResponse {
    
    private UUID voteId;
    private UUID candidateId;
    private UUID electionId;
    private Instant votedAt;
    private String message;
    private boolean success;
    
    public static VoteResponse success(UUID voteId, UUID candidateId, UUID electionId, Instant votedAt) {
        VoteResponse response = new VoteResponse();
        response.setVoteId(voteId);
        response.setCandidateId(candidateId);
        response.setElectionId(electionId);
        response.setVotedAt(votedAt);
        response.setMessage("Vote registered successfully");
        response.setSuccess(true);
        return response;
    }
    
    public static VoteResponse error(String message) {
        VoteResponse response = new VoteResponse();
        response.setMessage(message);
        response.setSuccess(false);
        return response;
    }
}
