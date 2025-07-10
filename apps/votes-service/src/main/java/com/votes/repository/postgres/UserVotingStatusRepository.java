package com.votes.repository.postgres;

import com.votes.entity.postgres.UserVotingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserVotingStatusRepository extends JpaRepository<UserVotingStatus, UUID> {
    
    Optional<UserVotingStatus> findByUserIdAndElectionId(UUID userId, UUID electionId);
    
    boolean existsByUserIdAndElectionIdAndHasVotedTrue(UUID userId, UUID electionId);
}
