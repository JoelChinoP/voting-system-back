package com.votes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VotingStatusResponse {
    
    private boolean hasVoted;
    private String message;
    
    public static VotingStatusResponse hasVoted() {
        return new VotingStatusResponse(true, "User has already voted");
    }
    
    public static VotingStatusResponse hasNotVoted() {
        return new VotingStatusResponse(false, "User has not voted yet");
    }
}
