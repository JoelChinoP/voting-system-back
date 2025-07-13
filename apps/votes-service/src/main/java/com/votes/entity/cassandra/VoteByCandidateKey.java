package com.votes.entity.cassandra;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import java.util.UUID;

@PrimaryKeyClass
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoteByCandidateKey {

    @PrimaryKeyColumn(name = "candidate_id", type = PrimaryKeyType.PARTITIONED)
    private UUID candidateId;

    @PrimaryKeyColumn(name = "election_id", type = PrimaryKeyType.CLUSTERED, ordering = Ordering.ASCENDING)
    private UUID electionId;

    @PrimaryKeyColumn(name = "vote_id", type = PrimaryKeyType.CLUSTERED, ordering = Ordering.ASCENDING)
    private UUID voteId;
}
