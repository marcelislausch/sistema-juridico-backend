package com.juridia.sistema.core.usecases;

import com.juridia.sistema.core.domain.Faturamento;
import com.juridia.sistema.infrastructure.persistence.FaturamentoRepository;
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
