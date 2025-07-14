package com.votes.entity.cassandra;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import java.util.UUID;

@PrimaryKeyClass
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserVoteLogKey {

    @PrimaryKeyColumn(name = "user_id", type = PrimaryKeyType.PARTITIONED)
    private UUID userId;

    @PrimaryKeyColumn(name = "election_id", type = PrimaryKeyType.CLUSTERED)
    private UUID electionId;
}
