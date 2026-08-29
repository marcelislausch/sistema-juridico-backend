package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Processo;
import com.sistemajuridico.backend.infrastructure.persistence.ProcessoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ListarProcessosPorClienteUseCase {

    private final ProcessoRepository processoRepository;

    public ListarProcessosPorClienteUseCase(ProcessoRepository processoRepository) {
        this.processoRepository = processoRepository;
    }

    public List<Processo> executar(UUID clienteId) {
        return processoRepository.findByClienteId(clienteId);
    }
}

