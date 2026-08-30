package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Processo;
import com.sistemajuridico.backend.core.domain.Tarefa;
import com.sistemajuridico.backend.core.domain.Usuario;
import com.sistemajuridico.backend.core.domain.exceptions.RecursoNaoEncontradoException;
import com.sistemajuridico.backend.core.domain.exceptions.RegraNegocioException;
import com.sistemajuridico.backend.infrastructure.persistence.ProcessoRepository;
import com.sistemajuridico.backend.infrastructure.persistence.TarefaRepository;
import com.sistemajuridico.backend.infrastructure.persistence.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class CriarTarefaUseCase {

    private final TarefaRepository tarefaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProcessoRepository processoRepository;

    public CriarTarefaUseCase(TarefaRepository tarefaRepository,
                              UsuarioRepository usuarioRepository,
                              ProcessoRepository processoRepository) {
        this.tarefaRepository = tarefaRepository;
        this.usuarioRepository = usuarioRepository;
        this.processoRepository = processoRepository;
    }

    public Tarefa executar(Tarefa tarefa, UUID usuarioId, UUID processoId) {
        if (tarefa.getDescricao() == null || tarefa.getDescricao().trim().isEmpty()) {
            throw new RegraNegocioException("A descrição da tarefa é obrigatória.");
        }

        if (tarefa.getDataVencimento() == null) {
            throw new RegraNegocioException("A data de vencimento da tarefa é obrigatória.");
        }

        if (usuarioId == null) {
            throw new RegraNegocioException("O usuário responsável pela tarefa é obrigatório.");
        }

        Optional<Usuario> optUsuario = this.usuarioRepository.findById(usuarioId);
        if (optUsuario.isEmpty()) {
            throw new RecursoNaoEncontradoException("Usuário responsável não encontrado!");
        }
        tarefa.setUsuario(optUsuario.get());

        if (processoId != null) {
            Optional<Processo> optProcesso = this.processoRepository.findById(processoId);
            if (optProcesso.isEmpty()) {
                throw new RecursoNaoEncontradoException("Processo associado não encontrado!");
            }
            tarefa.setProcesso(optProcesso.get());
        }

        tarefa.setConcluida(false);

        return this.tarefaRepository.save(tarefa);
    }
}
