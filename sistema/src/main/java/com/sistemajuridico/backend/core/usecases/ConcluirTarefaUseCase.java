package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Tarefa;
import com.sistemajuridico.backend.core.domain.exceptions.RecursoNaoEncontradoException;
import com.sistemajuridico.backend.infrastructure.persistence.TarefaRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ConcluirTarefaUseCase {

    private final TarefaRepository tarefaRepository;

    public ConcluirTarefaUseCase(TarefaRepository tarefaRepository) {
        this.tarefaRepository = tarefaRepository;
    }

    public Tarefa executar(UUID tarefaId) {
        Optional<Tarefa> optTarefa = this.tarefaRepository.findById(tarefaId);
        if (optTarefa.isEmpty()) {
            throw new RecursoNaoEncontradoException("Tarefa não encontrada no sistema!");
        }

        Tarefa tarefa = optTarefa.get();
        tarefa.setConcluida(true);

        return this.tarefaRepository.save(tarefa);
    }
}
