package com.votes.entity.cassandra;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("votes_by_candidate")
public class VoteByCandidate {
    
    @PrimaryKeyColumn(name = "candidate_id", type = PrimaryKeyType.PARTITIONED)
    private UUID candidateId;
    
    @PrimaryKeyColumn(name = "election_id", type = PrimaryKeyType.CLUSTERED, ordinal = 0)
    private UUID electionId;
    
    @PrimaryKeyColumn(name = "vote_id", type = PrimaryKeyType.CLUSTERED, ordinal = 1)
    private UUID voteId;
    
    @Column("voted_at")
    private Instant votedAt;
}
