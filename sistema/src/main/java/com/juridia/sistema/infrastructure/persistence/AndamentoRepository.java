package com.juridia.sistema.infrastructure.persistence;

import com.juridia.sistema.core.domain.Andamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AndamentoRepository extends JpaRepository<Andamento, UUID> {
    List<Andamento> findByProcessoIdOrderByDataHoraDesc(UUID processoId);
}

