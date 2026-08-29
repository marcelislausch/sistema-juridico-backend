package com.juridia.sistema.core.usecases;

import com.juridia.sistema.core.domain.Andamento;
import com.juridia.sistema.infrastructure.persistence.AndamentoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ListarAndamentosPorProcessoUseCase {

    private final AndamentoRepository andamentoRepository;

    public ListarAndamentosPorProcessoUseCase(AndamentoRepository andamentoRepository) {
        this.andamentoRepository = andamentoRepository;
    }

    public List<Andamento> executar(UUID processoId) {
        return andamentoRepository.findByProcessoIdOrderByDataHoraDesc(processoId);
    }
}
