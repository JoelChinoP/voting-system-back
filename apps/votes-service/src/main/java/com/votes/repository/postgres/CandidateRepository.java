package com.votes.repository.postgres;

import com.votes.entity.postgres.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, UUID> {

    /**
     * Busca candidatos activos para una elección específica
     */
    List<Candidate> findByElectionIdAndIsActiveTrue(UUID electionId);

    /**
     * Busca un candidato específico y verifica que esté activo
     */
    Optional<Candidate> findByIdAndIsActiveTrue(UUID candidateId);

    /**
     * Verifica si un candidato existe y está activo para una elección específica
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Candidate c WHERE c.id = :candidateId AND c.electionId = :electionId AND c.isActive = true")
    boolean existsActiveCandidateInElection(@Param("candidateId") UUID candidateId, @Param("electionId") UUID electionId);

    /**
     * Cuenta candidatos activos en una elección
     */
    long countByElectionIdAndIsActiveTrue(UUID electionId);
}
