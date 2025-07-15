package com.users.service;

import com.users.dto.CandidateRequest;
import com.users.dto.CandidateResponse;
import com.users.entity.Candidate;
import com.users.exception.CandidateAlreadyExistsException;
import com.users.exception.CandidateNotFoundException;
import com.users.repository.CandidateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CandidateService {

    private static final Logger log = LoggerFactory.getLogger(CandidateService.class);
    private final CandidateRepository candidateRepository;

    public CandidateService(CandidateRepository candidateRepository) {
        this.candidateRepository = candidateRepository;
    }

    @Transactional
    public CandidateResponse createCandidate(CandidateRequest request) {
        log.info("Creando candidato: {}", request.getNombre());

        // Verificar si ya existe un candidato con el mismo nombre y partido
        if (candidateRepository.existsByNombreIgnoreCaseAndPartidoPoliticoIgnoreCase(
                request.getNombre(), request.getPartidoPolitico())) {
            throw new CandidateAlreadyExistsException(
                "Ya existe un candidato con el nombre '" + request.getNombre() +
                "' en el partido '" + request.getPartidoPolitico() + "'"
            );
        }

        Candidate candidate = new Candidate();
        candidate.setNombre(request.getNombre().trim());
        candidate.setPartidoPolitico(request.getPartidoPolitico().trim());
        candidate.setCargo(request.getCargo() != null ? request.getCargo().trim() : null);
        candidate.setColor(request.getColor() != null ? request.getColor().trim() : null);
        candidate.setPropuestas(request.getPropuestas() != null ? request.getPropuestas().trim() : null);
        candidate.setExperiencia(request.getExperiencia() != null ? request.getExperiencia().trim() : null);
        candidate.setDescripcion(request.getDescripcion() != null ? request.getDescripcion().trim() : null);
        candidate.setEmail(request.getEmail() != null ? request.getEmail().trim() : null);
        candidate.setTelefono(request.getTelefono() != null ? request.getTelefono().trim() : null);
        candidate.setLugarNacimiento(request.getLugarNacimiento() != null ? request.getLugarNacimiento().trim() : null);
        candidate.setEducacion(request.getEducacion() != null ? request.getEducacion().trim() : null);
        candidate.setSitioWeb(request.getSitioWeb() != null ? request.getSitioWeb().trim() : null);
        candidate.setImagen(request.getImagen() != null ? request.getImagen().trim() : null);

        Candidate savedCandidate = candidateRepository.save(candidate);
        log.info("Candidato creado exitosamente con ID: {}", savedCandidate.getId());

        return mapToResponse(savedCandidate);
    }

    @Transactional(readOnly = true)
    public List<CandidateResponse> getAllCandidates() {
        log.info("Obteniendo todos los candidatos");
        return candidateRepository.findAllOrderByNombre()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CandidateResponse getCandidateById(Long id) {
        log.info("Obteniendo candidato con ID: {}", id);
        Candidate candidate = candidateRepository.findById(id)
                .orElseThrow(() -> new CandidateNotFoundException("Candidato no encontrado con ID: " + id));
        return mapToResponse(candidate);
    }

    @Transactional(readOnly = true)
    public List<CandidateResponse> getCandidatesByPartido(String partidoPolitico) {
        log.info("Obteniendo candidatos del partido: {}", partidoPolitico);
        return candidateRepository.findByPartidoPoliticoIgnoreCase(partidoPolitico)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CandidateResponse> searchCandidatesByName(String nombre) {
        log.info("Buscando candidatos por nombre: {}", nombre);
        return candidateRepository.findByNombreContainingIgnoreCase(nombre)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteCandidate(Long id) {
        log.info("Eliminando candidato con ID: {}", id);
        if (!candidateRepository.existsById(id)) {
            throw new CandidateNotFoundException("Candidato no encontrado con ID: " + id);
        }
        candidateRepository.deleteById(id);
        log.info("Candidato eliminado exitosamente con ID: {}", id);
    }

    @Transactional
    public CandidateResponse updateCandidate(Long id, CandidateRequest request) {
        log.info("Actualizando candidato con ID: {}", id);
        
        Candidate candidate = candidateRepository.findById(id)
                .orElseThrow(() -> new CandidateNotFoundException("Candidato no encontrado con ID: " + id));
        
        // Verificar si ya existe otro candidato con el mismo nombre y partido (excluyendo el actual)
        if (!candidate.getNombre().equalsIgnoreCase(request.getNombre()) || 
            !candidate.getPartidoPolitico().equalsIgnoreCase(request.getPartidoPolitico())) {
            
            if (candidateRepository.existsByNombreIgnoreCaseAndPartidoPoliticoIgnoreCaseAndIdNot(
                    request.getNombre(), request.getPartidoPolitico(), id)) {
                throw new CandidateAlreadyExistsException(
                    "Ya existe otro candidato con el nombre '" + request.getNombre() +
                    "' en el partido '" + request.getPartidoPolitico() + "'"
                );
            }
        }
        
        // Actualizar los campos
        candidate.setNombre(request.getNombre().trim());
        candidate.setPartidoPolitico(request.getPartidoPolitico().trim());
        candidate.setCargo(request.getCargo() != null ? request.getCargo().trim() : null);
        candidate.setColor(request.getColor() != null ? request.getColor().trim() : null);
        candidate.setPropuestas(request.getPropuestas() != null ? request.getPropuestas().trim() : null);
        candidate.setExperiencia(request.getExperiencia() != null ? request.getExperiencia().trim() : null);
        candidate.setDescripcion(request.getDescripcion() != null ? request.getDescripcion().trim() : null);
        candidate.setEmail(request.getEmail() != null ? request.getEmail().trim() : null);
        candidate.setTelefono(request.getTelefono() != null ? request.getTelefono().trim() : null);
        candidate.setLugarNacimiento(request.getLugarNacimiento() != null ? request.getLugarNacimiento().trim() : null);
        candidate.setEducacion(request.getEducacion() != null ? request.getEducacion().trim() : null);
        candidate.setSitioWeb(request.getSitioWeb() != null ? request.getSitioWeb().trim() : null);
        candidate.setImagen(request.getImagen() != null ? request.getImagen().trim() : null);
        
        Candidate savedCandidate = candidateRepository.save(candidate);
        log.info("Candidato actualizado exitosamente con ID: {}", savedCandidate.getId());
        
        return mapToResponse(savedCandidate);
    }

    private CandidateResponse mapToResponse(Candidate candidate) {
        return new CandidateResponse(
                candidate.getId(),
                candidate.getNombre(),
                candidate.getPartidoPolitico(),
                candidate.getCargo(),
                candidate.getColor(),
                candidate.getPropuestas(),
                candidate.getExperiencia(),
                candidate.getDescripcion(),
                candidate.getEmail(),
                candidate.getTelefono(),
                candidate.getLugarNacimiento(),
                candidate.getEducacion(),
                candidate.getSitioWeb(),
                candidate.getImagen(),
                candidate.getCreatedAt(),
                candidate.getUpdatedAt()
        );
    }
}
