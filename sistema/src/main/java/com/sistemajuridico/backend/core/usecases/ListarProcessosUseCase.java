package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Processo;
import com.sistemajuridico.backend.infrastructure.persistence.ProcessoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ListarProcessosUseCase {

    private final ProcessoRepository processoRepository;

    public ListarProcessosUseCase(ProcessoRepository processoRepository) {
        this.processoRepository = processoRepository;
    }

    public Page<Processo> executar(Pageable pageable) {
        return this.processoRepository.findAll(pageable);
    }
}
