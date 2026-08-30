package com.sistemajuridico.backend.infrastructure.persistence;

import com.sistemajuridico.backend.core.domain.Faturamento;
import com.sistemajuridico.backend.core.domain.enums.NaturezaFaturamentoEnum;
import com.sistemajuridico.backend.core.domain.enums.StatusFaturamentoEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface FaturamentoRepository extends JpaRepository<Faturamento, UUID> {
    List<Faturamento> findByProcessoId(UUID processoId);
    List<Faturamento> findByProcessoIdAndStatus(UUID processoId, StatusFaturamentoEnum status);
    List<Faturamento> findByStatusAndDataVencimentoAndNatureza(StatusFaturamentoEnum status, LocalDate dataVencimento, NaturezaFaturamentoEnum natureza);
    List<Faturamento> findByStatusAndNaturezaOrderByDataVencimentoAsc(StatusFaturamentoEnum status, NaturezaFaturamentoEnum natureza);
}
