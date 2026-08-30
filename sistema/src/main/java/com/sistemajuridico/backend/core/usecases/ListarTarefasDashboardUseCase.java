package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Tarefa;
import com.sistemajuridico.backend.core.domain.exceptions.RegraNegocioException;
import com.sistemajuridico.backend.infrastructure.persistence.TarefaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ListarTarefasDashboardUseCase {

    private final TarefaRepository tarefaRepository;

    public ListarTarefasDashboardUseCase(TarefaRepository tarefaRepository) {
        this.tarefaRepository = tarefaRepository;
    }

    public List<Tarefa> executar(UUID usuarioId) {
        if (usuarioId == null) {
            throw new RegraNegocioException("O identificador do usuário é obrigatório para listar tarefas do dashboard.");
        }

        return this.tarefaRepository.findByUsuarioIdAndConcluidaFalseOrderByDataVencimentoAsc(usuarioId);
    }
}
