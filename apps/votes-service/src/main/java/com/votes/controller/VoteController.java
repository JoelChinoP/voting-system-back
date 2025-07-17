package com.votes.controller;

import com.votes.dto.VoteRequest;
import com.votes.dto.VoteResponse;
import com.votes.dto.VotingStatusResponse;
import com.votes.service.VotingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@CrossOrigin(
        origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:4173"},
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS},
        allowCredentials = "true"
)
@RestController
@RequestMapping("/api/v1/votes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Votes", description = "Vote management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class VoteController {

    private final VotingService votingService;

    @Value("${app.default-election-id}")
    private String defaultElectionId;

    @PostMapping
    @Operation(summary = "Cast a vote", description = "Register a vote for a candidate")
    @ApiResponse(responseCode = "200", description = "Vote registered successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request or user already voted")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<VoteResponse> castVote(
            @Valid @RequestBody VoteRequest voteRequest,
            Authentication authentication) {

        try {
            UUID userId = (UUID) authentication.getPrincipal();
            log.info("Vote request received from user: {} for candidate: {}", 
                userId, voteRequest.getCandidateId());

            VoteResponse response = votingService.castVote(userId, voteRequest);

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            log.error("Error processing vote request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(VoteResponse.error("Internal server error"));
        }
    }

    @GetMapping("/status")
    @Operation(summary = "Check voting status", description = "Check if the current user has already voted")
    @ApiResponse(responseCode = "200", description = "Voting status retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<VotingStatusResponse> checkVotingStatus(
            @Parameter(description = "Election ID (optional, uses default if not provided)")
            @RequestParam(required = false) UUID electionId,
            Authentication authentication) {

        try {
            UUID userId = (UUID) authentication.getPrincipal();
            log.info("Checking voting status for user: {}", userId);

            if (electionId == null) {
                electionId = UUID.fromString(defaultElectionId);
            }

            VotingStatusResponse response = votingService.checkVotingStatus(userId, electionId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error checking voting status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new VotingStatusResponse(false, "Error checking status"));
        }
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the votes service is running")
    @ApiResponse(responseCode = "200", description = "Service is healthy")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Votes service is running");
    }
}
