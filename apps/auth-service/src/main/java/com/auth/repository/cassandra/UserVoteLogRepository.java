package com.auth.repository.cassandra;

import com.auth.entity.cassandra.UserVoteLog;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserVoteLogRepository extends CassandraRepository<UserVoteLog, UUID> {
    
    Optional<UserVoteLog> findByUserIdAndElectionId(UUID userId, UUID electionId);
    
    boolean existsByUserIdAndElectionId(UUID userId, UUID electionId);
}
