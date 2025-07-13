package com.votes.entity.cassandra;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("votes")
public class Vote {
    
    @PrimaryKey
    @Column("vote_id")
    private UUID voteId;
    
    @Column("candidate_id")
    private UUID candidateId;
    
    @Column("election_id")
    private UUID electionId;
    
    @Column("voted_at")
    private Instant votedAt;
    
    @Column("vote_hash")
    private String voteHash;
    
    @Column("metadata")
    private String metadata;
}
