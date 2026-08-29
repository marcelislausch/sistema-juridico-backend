package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Faturamento;
import com.sistemajuridico.backend.infrastructure.persistence.FaturamentoRepository;
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

    public List<Faturamento> buscarTodos() {
        return repository.findAll();
    }
}

