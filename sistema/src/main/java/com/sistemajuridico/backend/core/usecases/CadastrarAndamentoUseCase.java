package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Andamento;
import com.sistemajuridico.backend.core.domain.Processo;
import com.sistemajuridico.backend.core.domain.exceptions.RecursoNaoEncontradoException;
import com.sistemajuridico.backend.infrastructure.persistence.AndamentoRepository;
import com.sistemajuridico.backend.infrastructure.persistence.ProcessoRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class CadastrarAndamentoUseCase {

    private final AndamentoRepository andamentoRepository;
    private final ProcessoRepository processoRepository;

    public CadastrarAndamentoUseCase(AndamentoRepository andamentoRepository, ProcessoRepository processoRepository) {
        this.andamentoRepository = andamentoRepository;
        this.processoRepository = processoRepository;
    }

    public Andamento executar(Andamento andamento, UUID processoId) {
        Optional<Processo> optProcesso = processoRepository.findById(processoId);
        if (optProcesso.isEmpty()) {
            throw new RecursoNaoEncontradoException("Processo não encontrado no sistema!");
        }

        Processo processo = optProcesso.get();
        andamento.setProcesso(processo);
        return andamentoRepository.save(andamento);
    }
}
