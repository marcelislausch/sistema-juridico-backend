package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Tarefa;
import com.sistemajuridico.backend.core.domain.Usuario;
import com.sistemajuridico.backend.core.domain.enums.PerfilAcessoEnum;
import com.sistemajuridico.backend.core.domain.enums.TipoTarefaEnum;
import com.sistemajuridico.backend.core.domain.exceptions.RecursoNaoEncontradoException;
import com.sistemajuridico.backend.core.domain.exceptions.RegraNegocioException;
import com.sistemajuridico.backend.infrastructure.persistence.TarefaRepository;
import com.sistemajuridico.backend.infrastructure.persistence.UsuarioRepository;
import com.sistemajuridico.backend.presentation.dtos.GoogleCalendarEventDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class SincronizarEventoGoogleCalendarUseCase {

    private final TarefaRepository tarefaRepository;
    private final UsuarioRepository usuarioRepository;

    public SincronizarEventoGoogleCalendarUseCase(TarefaRepository tarefaRepository,
                                                 UsuarioRepository usuarioRepository) {
        this.tarefaRepository = tarefaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public Tarefa executar(GoogleCalendarEventDTO dto) {
        if (dto == null || dto.googleEventId() == null || dto.googleEventId().trim().isEmpty()) {
            throw new RegraNegocioException("O identificador do evento do Google Calendar (googleEventId) é obrigatório!");
        }

        String googleEventId = dto.googleEventId().trim();
        Optional<Tarefa> optTarefa = this.tarefaRepository.findByGoogleEventId(googleEventId);

        if (optTarefa.isPresent()) {
            Tarefa tarefaExistente = optTarefa.get();
            if (isCancelado(dto.status())) {
                this.tarefaRepository.delete(tarefaExistente);
                return null;
            } else {
                if (dto.descricao() != null && !dto.descricao().trim().isEmpty()) {
                    tarefaExistente.setDescricao(dto.descricao().trim());
                }
                if (dto.dataVencimento() != null) {
                    tarefaExistente.setDataVencimento(dto.dataVencimento());
                }
                return this.tarefaRepository.save(tarefaExistente);
            }
        } else {
            if (isCancelado(dto.status())) {
                return null;
            }

            Tarefa novaTarefa = new Tarefa();
            novaTarefa.setGoogleEventId(googleEventId);
            novaTarefa.setTipo(TipoTarefaEnum.ATENDIMENTO);

            String descricao = "Atendimento (Google Calendar)";
            if (dto.descricao() != null && !dto.descricao().trim().isEmpty()) {
                descricao = dto.descricao().trim();
            }
            novaTarefa.setDescricao(descricao);

            LocalDate dataVencimento = LocalDate.now();
            if (dto.dataVencimento() != null) {
                dataVencimento = dto.dataVencimento();
            }
            novaTarefa.setDataVencimento(dataVencimento);
            novaTarefa.setConcluida(false);

            Usuario responsavel = obterUsuarioResponsavel(dto);
            novaTarefa.setUsuario(responsavel);

            return this.tarefaRepository.save(novaTarefa);
        }
    }

    private boolean isCancelado(String status) {
        if (status == null) {
            return false;
        }
        String s = status.trim().toLowerCase();
        return s.equals("cancelled") || s.equals("canceled") || s.equals("cancelado") || s.equals("cancelada");
    }

    private Usuario obterUsuarioResponsavel(GoogleCalendarEventDTO dto) {
        if (dto.usuarioId() != null) {
            Optional<Usuario> opt = this.usuarioRepository.findById(dto.usuarioId());
            if (opt.isPresent()) {
                return opt.get();
            }
        }

        if (dto.usuarioEmail() != null && !dto.usuarioEmail().trim().isEmpty()) {
            Optional<Usuario> opt = this.usuarioRepository.findByEmail(dto.usuarioEmail().trim());
            if (opt.isPresent()) {
                return opt.get();
            }
        }

        List<Usuario> advogados = this.usuarioRepository.findByPerfilAndAtivoTrue(PerfilAcessoEnum.ADVOGADO);
        if (!advogados.isEmpty()) {
            return advogados.get(0);
        }

        List<Usuario> admins = this.usuarioRepository.findByPerfilAndAtivoTrue(PerfilAcessoEnum.ADMIN);
        if (!admins.isEmpty()) {
            return admins.get(0);
        }

        List<Usuario> todos = this.usuarioRepository.findAll();
        if (!todos.isEmpty()) {
            return todos.get(0);
        }

        throw new RecursoNaoEncontradoException("Nenhum usuário responsável encontrado no sistema para associar à tarefa!");
    }
}
