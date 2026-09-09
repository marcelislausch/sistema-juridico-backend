package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Audiencia;
import com.sistemajuridico.backend.core.domain.Faturamento;
import com.sistemajuridico.backend.core.domain.Tarefa;
import com.sistemajuridico.backend.core.domain.Usuario;
import com.sistemajuridico.backend.core.domain.enums.DestinoNotificacaoEnum;
import com.sistemajuridico.backend.core.domain.enums.NaturezaFaturamentoEnum;
import com.sistemajuridico.backend.core.domain.enums.PerfilAcessoEnum;
import com.sistemajuridico.backend.core.domain.enums.StatusFaturamentoEnum;
import com.sistemajuridico.backend.core.domain.enums.TipoNotificacaoEnum;
import com.sistemajuridico.backend.core.domain.enums.TipoRecursoNotificacaoEnum;
import com.sistemajuridico.backend.core.domain.exceptions.RecursoNaoEncontradoException;
import com.sistemajuridico.backend.core.domain.exceptions.RegraNegocioException;
import com.sistemajuridico.backend.infrastructure.persistence.AudienciaRepository;
import com.sistemajuridico.backend.infrastructure.persistence.FaturamentoRepository;
import com.sistemajuridico.backend.infrastructure.persistence.TarefaRepository;
import com.sistemajuridico.backend.infrastructure.persistence.UsuarioRepository;
import com.sistemajuridico.backend.presentation.dtos.NotificacaoItemDTO;
import com.sistemajuridico.backend.presentation.dtos.NotificacaoResumoDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ObterResumoNotificacoesUseCase {

    private final UsuarioRepository usuarioRepository;
    private final AudienciaRepository audienciaRepository;
    private final TarefaRepository tarefaRepository;
    private final FaturamentoRepository faturamentoRepository;

    public ObterResumoNotificacoesUseCase(UsuarioRepository usuarioRepository,
                                          AudienciaRepository audienciaRepository,
                                          TarefaRepository tarefaRepository,
                                          FaturamentoRepository faturamentoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.audienciaRepository = audienciaRepository;
        this.tarefaRepository = tarefaRepository;
        this.faturamentoRepository = faturamentoRepository;
    }

    @Transactional(readOnly = true)
    public NotificacaoResumoDTO executar(String emailUsuario, LocalDate dataReferencia) {
        if (emailUsuario == null || emailUsuario.trim().isEmpty()) {
            throw new RegraNegocioException("Identificação do usuário autenticado ausente!");
        }

        Optional<Usuario> optUsuario = this.usuarioRepository.findByEmail(emailUsuario.trim());
        if (optUsuario.isEmpty()) {
            throw new RecursoNaoEncontradoException("Usuário logado não encontrado no sistema!");
        }

        Usuario usuario = optUsuario.get();
        LocalDate data = dataReferencia != null ? dataReferencia : LocalDate.now();

        LocalDateTime inicioDia = data.atStartOfDay();
        LocalDateTime fimDia = data.atTime(LocalTime.MAX);

        List<NotificacaoItemDTO> itens = new ArrayList<>();
        int quantidadeAgenda = 0;
        int quantidadeFinanceiro = 0;

        // 1. Audiências do Dia
        List<Audiencia> audienciasDoDia = this.audienciaRepository.findByDataHoraBetweenOrderByDataHoraAsc(inicioDia, fimDia);
        for (Audiencia audiencia : audienciasDoDia) {
            boolean podeVisualizar = false;
            if (usuario.getPerfil() == PerfilAcessoEnum.ADMIN || usuario.getPerfil() == PerfilAcessoEnum.SECRETARIA) {
                podeVisualizar = true;
            } else if (audiencia.getProcesso() == null || audiencia.getProcesso().getAdvogado() == null) {
                podeVisualizar = true;
            } else if (usuario.getId().equals(audiencia.getProcesso().getAdvogado().getId())) {
                podeVisualizar = true;
            }

            if (podeVisualizar) {
                String titulo = "Audiência agendada";
                if (audiencia.getProcesso() != null && audiencia.getProcesso().getNumeroCnj() != null) {
                    titulo = "Audiência: " + audiencia.getProcesso().getNumeroCnj();
                }

                String descricao = audiencia.getLocal();
                if (audiencia.getObservacoes() != null && !audiencia.getObservacoes().trim().isEmpty()) {
                    descricao = descricao + " - " + audiencia.getObservacoes().trim();
                }

                String horario = String.format("%02d:%02d",
                        audiencia.getDataHora().getHour(),
                        audiencia.getDataHora().getMinute());

                NotificacaoItemDTO item = new NotificacaoItemDTO(
                        audiencia.getId(),
                        TipoNotificacaoEnum.AUDIENCIA,
                        titulo,
                        descricao,
                        horario,
                        DestinoNotificacaoEnum.AUDIENCIAS,
                        TipoRecursoNotificacaoEnum.AUDIENCIA,
                        audiencia.getId()
                );
                itens.add(item);
                quantidadeAgenda++;
            }
        }

        // 2. Tarefas do Dia vinculadas ao Usuário
        List<Tarefa> tarefasPeriodo = this.tarefaRepository.findByUsuarioIdAndDataVencimentoBetween(usuario.getId(), data, data);
        for (Tarefa tarefa : tarefasPeriodo) {
            boolean concluida = Boolean.TRUE.equals(tarefa.getConcluida());
            if (!concluida) {
                String titulo = "Prazo de Tarefa";
                if (tarefa.getTipo() != null) {
                    titulo = "Tarefa: " + tarefa.getTipo().name();
                }

                NotificacaoItemDTO item = new NotificacaoItemDTO(
                        tarefa.getId(),
                        TipoNotificacaoEnum.TAREFA,
                        titulo,
                        tarefa.getDescricao(),
                        null,
                        DestinoNotificacaoEnum.AGENDA,
                        TipoRecursoNotificacaoEnum.TAREFA,
                        tarefa.getId()
                );
                itens.add(item);
                quantidadeAgenda++;
            }
        }

        // 3. Faturamentos / Obrigações do Dia (Apenas ADMIN e SECRETARIA)
        if (usuario.getPerfil() == PerfilAcessoEnum.ADMIN || usuario.getPerfil() == PerfilAcessoEnum.SECRETARIA) {
            List<Faturamento> faturamentosDoDia = this.faturamentoRepository.findByStatusAndDataVencimento(StatusFaturamentoEnum.PENDENTE, data);
            for (Faturamento faturamento : faturamentosDoDia) {
                String titulo = "Vencimento Financeiro";
                if (faturamento.getNatureza() == NaturezaFaturamentoEnum.A_RECEBER) {
                    titulo = "Fatura a Receber";
                } else if (faturamento.getNatureza() == NaturezaFaturamentoEnum.A_PAGAR) {
                    titulo = "Despesa a Pagar";
                }

                String descricao = faturamento.getDescricao();
                if (faturamento.getValor() != null) {
                    descricao = descricao + " - R$ " + faturamento.getValor();
                }

                NotificacaoItemDTO item = new NotificacaoItemDTO(
                        faturamento.getId(),
                        TipoNotificacaoEnum.FINANCEIRO,
                        titulo,
                        descricao,
                        null,
                        DestinoNotificacaoEnum.FINANCEIRO,
                        TipoRecursoNotificacaoEnum.FATURAMENTO,
                        faturamento.getId()
                );
                itens.add(item);
                quantidadeFinanceiro++;
            }
        }

        return new NotificacaoResumoDTO(itens, quantidadeAgenda, quantidadeFinanceiro);
    }
}
