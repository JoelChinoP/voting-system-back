package com.auth.entity.cassandra;

import lombok.AllArgsConstructor;
import lombok.Builder;
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
@Builder
@Table("user_votes_log")
public class UserVoteLog {
    
    @PrimaryKeyColumn(name = "user_id", type = PrimaryKeyType.PARTITIONED)
    private UUID userId;
    
    @PrimaryKeyColumn(name = "election_id", type = PrimaryKeyType.CLUSTERED)
    private UUID electionId;
    
    @Column("vote_id")
    private UUID voteId;
    
    @Column("candidate_id")
    private UUID candidateId;
    
    @Column("voted_at")
    private Instant votedAt;
}
