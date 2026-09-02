package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Tarefa;
import com.sistemajuridico.backend.core.domain.exceptions.RecursoNaoEncontradoException;
import com.sistemajuridico.backend.infrastructure.persistence.TarefaRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AtualizarTarefaUseCase {

    private final TarefaRepository tarefaRepository;

    public AtualizarTarefaUseCase(TarefaRepository tarefaRepository) {
        this.tarefaRepository = tarefaRepository;
    }

    public Tarefa executar(UUID id, Tarefa dadosAtualizados) {
        Optional<Tarefa> optTarefa = this.tarefaRepository.findById(id);
        if (optTarefa.isEmpty()) {
            throw new RecursoNaoEncontradoException("Tarefa não encontrada no sistema!");
        }

        Tarefa tarefaExistente = optTarefa.get();
        tarefaExistente.setDescricao(dadosAtualizados.getDescricao());
        tarefaExistente.setDataVencimento(dadosAtualizados.getDataVencimento());
        tarefaExistente.setTipo(dadosAtualizados.getTipo());

        return this.tarefaRepository.save(tarefaExistente);
    }
}
