package com.votes.entity.postgres;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_voting_status")
public class UserVotingStatus {
    
    @Id
    @Column(name = "user_id")
    private UUID userId;
    
    @Column(name = "election_id", nullable = false)
    private UUID electionId;
    
    @Column(name = "has_voted", nullable = false)
    private Boolean hasVoted = false;
    
    @Column(name = "voted_at")
    private Instant votedAt;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
