package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Andamento;
import com.sistemajuridico.backend.infrastructure.persistence.AndamentoRepository;
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

