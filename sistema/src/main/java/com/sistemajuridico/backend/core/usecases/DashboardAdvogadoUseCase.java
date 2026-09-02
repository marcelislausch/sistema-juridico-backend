package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Audiencia;
import com.sistemajuridico.backend.core.domain.Faturamento;
import com.sistemajuridico.backend.core.domain.Tarefa;
import com.sistemajuridico.backend.core.domain.enums.FaseProcessualEnum;
import com.sistemajuridico.backend.core.domain.enums.NaturezaFaturamentoEnum;
import com.sistemajuridico.backend.core.domain.enums.StatusFaturamentoEnum;
import com.sistemajuridico.backend.infrastructure.persistence.AudienciaRepository;
import com.sistemajuridico.backend.infrastructure.persistence.ClienteRepository;
import com.sistemajuridico.backend.infrastructure.persistence.FaturamentoRepository;
import com.sistemajuridico.backend.infrastructure.persistence.ProcessoRepository;
import com.sistemajuridico.backend.infrastructure.persistence.TarefaRepository;
import com.sistemajuridico.backend.presentation.dtos.AudienciaDTO;
import com.sistemajuridico.backend.presentation.dtos.FaturamentoDTO;
import com.sistemajuridico.backend.presentation.dtos.ResumoDashboardDTO;
import com.sistemajuridico.backend.presentation.dtos.TarefaDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DashboardAdvogadoUseCase {

    private final ClienteRepository clienteRepository;
    private final ProcessoRepository processoRepository;
    private final TarefaRepository tarefaRepository;
    private final FaturamentoRepository faturamentoRepository;
    private final AudienciaRepository audienciaRepository;

    public DashboardAdvogadoUseCase(ClienteRepository clienteRepository,
                                    ProcessoRepository processoRepository,
                                    TarefaRepository tarefaRepository,
                                    FaturamentoRepository faturamentoRepository,
                                    AudienciaRepository audienciaRepository) {
        this.clienteRepository = clienteRepository;
        this.processoRepository = processoRepository;
        this.tarefaRepository = tarefaRepository;
        this.faturamentoRepository = faturamentoRepository;
        this.audienciaRepository = audienciaRepository;
    }

    public ResumoDashboardDTO executar(UUID usuarioId) {
        int totalClientesAtivos = this.clienteRepository.findAll().size();
        int totalProcessosAndamento = this.processoRepository.countByAdvogadoIdAndFaseAtualNot(usuarioId, FaseProcessualEnum.ARQUIVADO);

        List<Tarefa> pendentes = this.tarefaRepository.findByUsuarioIdAndConcluidaFalseOrderByDataVencimentoAsc(usuarioId);

        LocalDate hoje = LocalDate.now();
        int tarefasPendentesHoje = 0;
        for (Tarefa tarefa : pendentes) {
            if (hoje.equals(tarefa.getDataVencimento())) {
                tarefasPendentesHoje++;
            }
        }

        List<TarefaDTO> proximasTarefas = new ArrayList<>();
        int limiteTarefas = Math.min(5, pendentes.size());
        for (int i = 0; i < limiteTarefas; i++) {
            proximasTarefas.add(TarefaDTO.fromEntity(pendentes.get(i)));
        }

        List<Faturamento> faturasReceber = this.faturamentoRepository.findByStatusAndNaturezaOrderByDataVencimentoAsc(
                StatusFaturamentoEnum.PENDENTE,
                NaturezaFaturamentoEnum.A_RECEBER
        );

        BigDecimal totalReceberHoje = BigDecimal.ZERO;
        for (Faturamento faturamento : faturasReceber) {
            if (hoje.equals(faturamento.getDataVencimento()) && faturamento.getValor() != null) {
                totalReceberHoje = totalReceberHoje.add(faturamento.getValor());
            }
        }

        List<FaturamentoDTO> proximasFaturasReceber = new ArrayList<>();
        int limiteFaturas = Math.min(5, faturasReceber.size());
        for (int i = 0; i < limiteFaturas; i++) {
            proximasFaturasReceber.add(FaturamentoDTO.fromEntity(faturasReceber.get(i)));
        }

        LocalDateTime inicioHoje = hoje.atStartOfDay();
        LocalDateTime fimHoje = hoje.atTime(LocalTime.MAX);
        List<Audiencia> audienciasHojeList = this.audienciaRepository.findByDataHoraBetweenOrderByDataHoraAsc(inicioHoje, fimHoje);
        int audienciasHoje = audienciasHojeList.size();

        LocalDateTime fimSeteDias = hoje.plusDays(7).atTime(LocalTime.MAX);
        List<Audiencia> proximasAudienciasEntities = this.audienciaRepository.findByDataHoraBetweenOrderByDataHoraAsc(inicioHoje, fimSeteDias);
        List<AudienciaDTO> proximasAudiencias = new ArrayList<>();
        for (Audiencia audiencia : proximasAudienciasEntities) {
            proximasAudiencias.add(AudienciaDTO.fromEntity(audiencia));
        }

        return new ResumoDashboardDTO(
                totalClientesAtivos,
                totalProcessosAndamento,
                tarefasPendentesHoje,
                proximasTarefas,
                totalReceberHoje,
                proximasFaturasReceber,
                audienciasHoje,
                proximasAudiencias
        );
    }
}
