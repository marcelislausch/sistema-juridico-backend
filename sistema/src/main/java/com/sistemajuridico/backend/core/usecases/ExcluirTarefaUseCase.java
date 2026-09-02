package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Tarefa;
import com.sistemajuridico.backend.core.domain.exceptions.RecursoNaoEncontradoException;
import com.sistemajuridico.backend.infrastructure.persistence.TarefaRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ExcluirTarefaUseCase {

    private final TarefaRepository tarefaRepository;

    public ExcluirTarefaUseCase(TarefaRepository tarefaRepository) {
        this.tarefaRepository = tarefaRepository;
    }

    public void executar(UUID id) {
        Optional<Tarefa> optTarefa = this.tarefaRepository.findById(id);
        if (optTarefa.isEmpty()) {
            throw new RecursoNaoEncontradoException("Tarefa não encontrada no sistema!");
        }

        this.tarefaRepository.delete(optTarefa.get());
    }
}
