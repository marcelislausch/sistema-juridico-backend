package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Processo;
import com.sistemajuridico.backend.core.domain.exceptions.RecursoNaoEncontradoException;
import com.sistemajuridico.backend.infrastructure.persistence.ProcessoRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class BuscarProcessoPorIdUseCase {

    private final ProcessoRepository processoRepository;

    public BuscarProcessoPorIdUseCase(ProcessoRepository processoRepository) {
        this.processoRepository = processoRepository;
    }

    public Processo executar(UUID id) {
        Optional<Processo> optProcesso = this.processoRepository.findById(id);
        if (optProcesso.isEmpty()) {
            throw new RecursoNaoEncontradoException("Processo não encontrado no sistema!");
        }
        return optProcesso.get();
    }
}
