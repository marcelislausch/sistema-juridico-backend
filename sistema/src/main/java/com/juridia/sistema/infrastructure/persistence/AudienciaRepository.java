package com.juridia.sistema.infrastructure.persistence;

import com.juridia.sistema.core.domain.Audiencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AudienciaRepository extends JpaRepository<Audiencia, UUID> {
    List<Audiencia> findByProcessoId(UUID processoId);
}