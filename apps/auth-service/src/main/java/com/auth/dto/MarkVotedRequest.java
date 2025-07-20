package com.auth.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class MarkVotedRequest {
    private UUID electionId;
    private UUID voteId;
}
