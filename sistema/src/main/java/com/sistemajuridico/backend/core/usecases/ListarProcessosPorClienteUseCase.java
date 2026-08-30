package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Processo;
import com.sistemajuridico.backend.infrastructure.persistence.ProcessoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ListarProcessosPorClienteUseCase {

    private final ProcessoRepository processoRepository;

    public ListarProcessosPorClienteUseCase(ProcessoRepository processoRepository) {
        this.processoRepository = processoRepository;
    }

    public Page<Processo> executar(UUID clienteId, Pageable pageable) {
        return processoRepository.findByClienteId(clienteId, pageable);
    }
}
