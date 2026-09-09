package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Faturamento;
import com.sistemajuridico.backend.core.domain.enums.NaturezaFaturamentoEnum;
import com.sistemajuridico.backend.core.domain.enums.StatusFaturamentoEnum;
import com.sistemajuridico.backend.infrastructure.persistence.FaturamentoRepository;
import com.sistemajuridico.backend.core.domain.enums.TipoFaturamentoEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class ListarFaturamentosUseCase {

    private final FaturamentoRepository repository;

    public ListarFaturamentosUseCase(FaturamentoRepository repository) {
        this.repository = repository;
    }

    public List<Faturamento> buscarPorProcesso(UUID processoId) {
        return repository.findByProcessoId(processoId);
    }

    public Page<Faturamento> buscarComFiltros(String q,
                                             StatusFaturamentoEnum status,
                                             NaturezaFaturamentoEnum natureza,
                                             TipoFaturamentoEnum tipo,
                                             LocalDate vencimentoDe,
                                             LocalDate vencimentoAte,
                                             UUID processoId,
                                             Pageable pageable) {
        String statusStr = null;
        if (status != null) {
            statusStr = status.name();
        }
        String naturezaStr = null;
        if (natureza != null) {
            naturezaStr = natureza.name();
        }
        String tipoStr = null;
        if (tipo != null) {
            tipoStr = tipo.name();
        }
        return this.repository.buscarComFiltros(q, statusStr, naturezaStr, tipoStr, vencimentoDe, vencimentoAte, processoId, pageable);
    }

    public Page<Faturamento> buscarTodos(StatusFaturamentoEnum status, NaturezaFaturamentoEnum natureza, Pageable pageable) {
        return buscarComFiltros(null, status, natureza, null, null, null, null, pageable);
    }

    public List<Faturamento> buscarTodos() {
        return repository.findAll();
    }
}

