package com.votes.repository.cassandra;

import com.votes.entity.cassandra.UserVoteLog;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserVoteLogRepository extends CassandraRepository<UserVoteLog, UUID> {
    
    Optional<UserVoteLog> findByUserIdAndElectionId(UUID userId, UUID electionId);
}
