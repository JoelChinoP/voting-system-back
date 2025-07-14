package com.votes.repository.cassandra;

import com.votes.entity.cassandra.VoteByCandidate;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VoteByCandidateRepository extends CassandraRepository<VoteByCandidate, UUID> {
    
    List<VoteByCandidate> findByCandidateIdAndElectionId(UUID candidateId, UUID electionId);
}
