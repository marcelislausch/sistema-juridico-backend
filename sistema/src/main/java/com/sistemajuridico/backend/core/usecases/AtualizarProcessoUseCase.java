package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Processo;
import com.sistemajuridico.backend.core.domain.exceptions.RecursoNaoEncontradoException;
import com.sistemajuridico.backend.infrastructure.persistence.ProcessoRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AtualizarProcessoUseCase {

    private final ProcessoRepository processoRepository;

    public AtualizarProcessoUseCase(ProcessoRepository processoRepository) {
        this.processoRepository = processoRepository;
    }

    public Processo executar(UUID id, Processo dadosAtualizados) {
        Optional<Processo> optProcesso = this.processoRepository.findById(id);
        if (optProcesso.isEmpty()) {
            throw new RecursoNaoEncontradoException("Processo não encontrado no sistema!");
        }

        Processo processoExistente = optProcesso.get();
        processoExistente.setAssunto(dadosAtualizados.getAssunto());
        processoExistente.setFaseAtual(dadosAtualizados.getFaseAtual());

        return this.processoRepository.save(processoExistente);
    }
}
