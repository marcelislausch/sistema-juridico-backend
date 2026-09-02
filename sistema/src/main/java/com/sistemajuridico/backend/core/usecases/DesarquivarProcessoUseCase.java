package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Processo;
import com.sistemajuridico.backend.core.domain.enums.FaseProcessualEnum;
import com.sistemajuridico.backend.core.domain.exceptions.RecursoNaoEncontradoException;
import com.sistemajuridico.backend.infrastructure.persistence.ProcessoRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class DesarquivarProcessoUseCase {

    private final ProcessoRepository processoRepository;

    public DesarquivarProcessoUseCase(ProcessoRepository processoRepository) {
        this.processoRepository = processoRepository;
    }

    public Processo executar(UUID processoId) {
        Optional<Processo> optProcesso = this.processoRepository.findById(processoId);
        if (optProcesso.isEmpty()) {
            throw new RecursoNaoEncontradoException("Processo não encontrado no sistema!");
        }

        Processo processo = optProcesso.get();
        processo.setFaseAtual(FaseProcessualEnum.EM_ANDAMENTO);

        return this.processoRepository.save(processo);
    }
}
