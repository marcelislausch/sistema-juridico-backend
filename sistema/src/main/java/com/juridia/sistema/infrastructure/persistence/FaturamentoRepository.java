package com.juridia.sistema.infrastructure.persistence;

import com.juridia.sistema.core.domain.Faturamento;
import com.juridia.sistema.core.domain.enums.StatusFaturamentoEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FaturamentoRepository extends JpaRepository<Faturamento, UUID> {
    List<Faturamento> findByProcessoId(UUID processoId);
    List<Faturamento> findByProcessoIdAndStatus(UUID processoId, StatusFaturamentoEnum status);
}
