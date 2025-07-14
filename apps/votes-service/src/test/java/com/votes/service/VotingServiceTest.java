package com.votes.service;

import com.votes.dto.VoteRequest;
import com.votes.dto.VoteResponse;
import com.votes.repository.cassandra.UserVoteLogRepository;
import com.votes.repository.cassandra.VoteByCandidateRepository;
import com.votes.repository.cassandra.VoteRepository;
import com.votes.repository.postgres.UserVotingStatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VotingServiceTest {

    @Mock
    private VoteRepository voteRepository;

    @Mock
    private VoteByCandidateRepository voteByCandidateRepository;

    @Mock
    private UserVoteLogRepository userVoteLogRepository;

    @Mock
    private UserVotingStatusRepository userVotingStatusRepository;

    @InjectMocks
    private VotingService votingService;

    private UUID userId;
    private UUID candidateId;
    private UUID electionId;
    private VoteRequest voteRequest;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        candidateId = UUID.randomUUID();
        electionId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        
        voteRequest = new VoteRequest();
        voteRequest.setCandidateId(candidateId);
        voteRequest.setElectionId(electionId);

        // Set the default election ID
        ReflectionTestUtils.setField(votingService, "defaultElectionId", electionId.toString());
    }

    @Test
    void testCastVote_Success() {
        // Given
        when(userVotingStatusRepository.existsByUserIdAndElectionIdAndHasVotedTrue(userId, electionId))
                .thenReturn(false);
        when(userVoteLogRepository.findByUserIdAndElectionId(userId, electionId))
                .thenReturn(java.util.Optional.empty());

        // When
        VoteResponse response = votingService.castVote(userId, voteRequest);

        // Then
        assertTrue(response.isSuccess());
        assertEquals("Vote registered successfully", response.getMessage());
        assertNotNull(response.getVoteId());
        assertEquals(candidateId, response.getCandidateId());

        verify(voteRepository).save(any());
        verify(voteByCandidateRepository).save(any());
        verify(userVoteLogRepository).save(any());
        verify(userVotingStatusRepository).save(any());
    }

    @Test
    void testCastVote_UserAlreadyVoted() {
        // Given
        when(userVotingStatusRepository.existsByUserIdAndElectionIdAndHasVotedTrue(userId, electionId))
                .thenReturn(true);

        // When
        VoteResponse response = votingService.castVote(userId, voteRequest);

        // Then
        assertFalse(response.isSuccess());
        assertEquals("User has already voted in this election", response.getMessage());

        verify(voteRepository, never()).save(any());
        verify(voteByCandidateRepository, never()).save(any());
        verify(userVoteLogRepository, never()).save(any());
        verify(userVotingStatusRepository, never()).save(any());
    }
}
