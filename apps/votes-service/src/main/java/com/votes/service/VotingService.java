package com.votes.service;

import com.votes.dto.VoteRequest;
import com.votes.dto.VoteResponse;
import com.votes.dto.VotingStatusResponse;
import com.votes.entity.cassandra.UserVoteLog;
import com.votes.entity.cassandra.Vote;
import com.votes.entity.cassandra.VoteByCandidate;
import com.votes.entity.postgres.UserVotingStatus;
import com.votes.repository.cassandra.UserVoteLogRepository;
import com.votes.repository.cassandra.VoteByCandidateRepository;
import com.votes.repository.cassandra.VoteRepository;
import com.votes.repository.postgres.UserVotingStatusRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VotingService {

    private final VoteRepository voteRepository;
    private final VoteByCandidateRepository voteByCandidateRepository;
    private final UserVoteLogRepository userVoteLogRepository;
    private final UserVotingStatusRepository userVotingStatusRepository;

    @Value("${app.default-election-id}")
    private String defaultElectionId;

    @Transactional
    public VoteResponse castVote(UUID userId, VoteRequest voteRequest) {
        try {
            UUID electionId = voteRequest.getElectionId() != null ? 
                voteRequest.getElectionId() : UUID.fromString(defaultElectionId);

            log.info("Processing vote for user {} in election {} for candidate {}", 
                userId, electionId, voteRequest.getCandidateId());

            // 1. Verificar si el usuario ya votó
            if (hasUserVoted(userId, electionId)) {
                log.warn("User {} already voted in election {}", userId, electionId);
                return VoteResponse.error("User has already voted in this election");
            }

            // 2. Registrar voto anónimo en Cassandra
            UUID voteId = UUID.randomUUID();
            Instant votedAt = Instant.now();
            
            String voteHash = generateVoteHash(voteId, voteRequest.getCandidateId(), electionId, votedAt);

            // Guardar voto principal
            Vote vote = new Vote();
            vote.setVoteId(voteId);
            vote.setCandidateId(voteRequest.getCandidateId());
            vote.setElectionId(electionId);
            vote.setVotedAt(votedAt);
            vote.setVoteHash(voteHash);
            vote.setMetadata("{}"); // JSON metadata vacío por ahora
            
            voteRepository.save(vote);
            log.info("Vote saved in main table: {}", voteId);

            // Guardar para conteo por candidato
            VoteByCandidate voteByCandidate = new VoteByCandidate();
            voteByCandidate.setCandidateId(voteRequest.getCandidateId());
            voteByCandidate.setElectionId(electionId);
            voteByCandidate.setVoteId(voteId);
            voteByCandidate.setVotedAt(votedAt);
            
            voteByCandidateRepository.save(voteByCandidate);
            log.info("Vote saved in candidate table for candidate: {}", voteRequest.getCandidateId());

            // Guardar log de usuario (para verificar duplicados)
            UserVoteLog userVoteLog = new UserVoteLog();
            userVoteLog.setUserId(userId);
            userVoteLog.setElectionId(electionId);
            userVoteLog.setVoteId(voteId);
            userVoteLog.setCandidateId(voteRequest.getCandidateId());
            userVoteLog.setVotedAt(votedAt);
            
            userVoteLogRepository.save(userVoteLog);
            log.info("User vote log saved for user: {}", userId);

            // 3. Marcar usuario como votado en PostgreSQL
            markUserAsVoted(userId, electionId, votedAt);
            log.info("User {} marked as voted in PostgreSQL", userId);

            return VoteResponse.success(voteId, voteRequest.getCandidateId(), electionId, votedAt);

        } catch (Exception e) {
            log.error("Error casting vote for user {}: {}", userId, e.getMessage(), e);
            return VoteResponse.error("Error processing vote: " + e.getMessage());
        }
    }

    public VotingStatusResponse checkVotingStatus(UUID userId, UUID electionId) {
        if (electionId == null) {
            electionId = UUID.fromString(defaultElectionId);
        }

        boolean hasVoted = hasUserVoted(userId, electionId);
        return hasVoted ? VotingStatusResponse.hasVoted() : VotingStatusResponse.hasNotVoted();
    }

    private boolean hasUserVoted(UUID userId, UUID electionId) {
        // Verificar primero en PostgreSQL (más rápido)
        boolean votedInPostgres = userVotingStatusRepository.existsByUserIdAndElectionIdAndHasVotedTrue(userId, electionId);
        
        if (votedInPostgres) {
            return true;
        }

        // Verificar en Cassandra como backup
        return userVoteLogRepository.findByUserIdAndElectionId(userId, electionId).isPresent();
    }

    private void markUserAsVoted(UUID userId, UUID electionId, Instant votedAt) {
        UserVotingStatus userStatus = userVotingStatusRepository
                .findByUserIdAndElectionId(userId, electionId)
                .orElse(new UserVotingStatus());

        userStatus.setUserId(userId);
        userStatus.setElectionId(electionId);
        userStatus.setHasVoted(true);
        userStatus.setVotedAt(votedAt);

        if (userStatus.getCreatedAt() == null) {
            userStatus.setCreatedAt(Instant.now());
        }
        userStatus.setUpdatedAt(Instant.now());

        userVotingStatusRepository.save(userStatus);
    }

    private String generateVoteHash(UUID voteId, UUID candidateId, UUID electionId, Instant votedAt) {
        try {
            String input = voteId.toString() + candidateId.toString() + 
                          electionId.toString() + votedAt.toString();
            
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("Error generating vote hash", e);
            return UUID.randomUUID().toString();
        }
    }
}
