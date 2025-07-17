package com.users.repository;

import com.users.entity.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    @Query("SELECT c FROM Candidate c WHERE LOWER(c.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Candidate> findByNombreContainingIgnoreCase(@Param("nombre") String nombre);

    @Query("SELECT c FROM Candidate c WHERE LOWER(c.partidoPolitico) = LOWER(:partido)")
    List<Candidate> findByPartidoPoliticoIgnoreCase(@Param("partido") String partidoPolitico);

    @Query("SELECT c FROM Candidate c ORDER BY c.nombre ASC")
    List<Candidate> findAllOrderByNombre();

    boolean existsByNombreIgnoreCaseAndPartidoPoliticoIgnoreCase(String nombre, String partidoPolitico);

    boolean existsByNombreIgnoreCaseAndPartidoPoliticoIgnoreCaseAndIdNot(String nombre, String partidoPolitico, Long id);

}
