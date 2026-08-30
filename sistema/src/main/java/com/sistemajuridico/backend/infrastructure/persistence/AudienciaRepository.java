package com.sistemajuridico.backend.infrastructure.persistence;

import com.sistemajuridico.backend.core.domain.Audiencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AudienciaRepository extends JpaRepository<Audiencia, UUID> {
    List<Audiencia> findByProcessoId(UUID processoId);
    List<Audiencia> findByDataHoraBetween(LocalDateTime inicio, LocalDateTime fim);
}
