package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Faturamento;
import com.sistemajuridico.backend.core.domain.enums.NaturezaFaturamentoEnum;
import com.sistemajuridico.backend.core.domain.enums.StatusFaturamentoEnum;
import com.sistemajuridico.backend.infrastructure.persistence.FaturamentoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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

    public Page<Faturamento> buscarTodos(StatusFaturamentoEnum status, NaturezaFaturamentoEnum natureza, Pageable pageable) {
        if (status != null && natureza != null) {
            return this.repository.findByStatusAndNatureza(status, natureza, pageable);
        } else if (status != null) {
            return this.repository.findByStatus(status, pageable);
        } else if (natureza != null) {
            return this.repository.findByNatureza(natureza, pageable);
        } else {
            return this.repository.findAll(pageable);
        }
    }

    public List<Faturamento> buscarTodos() {
        return repository.findAll();
    }
}

