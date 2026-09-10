package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.*;
import com.sistemajuridico.backend.core.domain.enums.FaseProcessualEnum;
import com.sistemajuridico.backend.core.domain.enums.NaturezaFaturamentoEnum;
import com.sistemajuridico.backend.core.domain.enums.StatusAudienciaEnum;
import com.sistemajuridico.backend.core.domain.enums.StatusFaturamentoEnum;
import com.sistemajuridico.backend.infrastructure.persistence.*;
import com.sistemajuridico.backend.presentation.dtos.ResumoDashboardDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardAdvogadoUseCaseTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ProcessoRepository processoRepository;

    @Mock
    private TarefaRepository tarefaRepository;

    @Mock
    private FaturamentoRepository faturamentoRepository;

    @Mock
    private AudienciaRepository audienciaRepository;

    @InjectMocks
    private DashboardAdvogadoUseCase dashboardAdvogadoUseCase;

    @Test
    void shouldExecuteDashboardPopulatingProcessoResumoDTOImperatively() {
        UUID usuarioId = UUID.randomUUID();
        UUID processoId = UUID.randomUUID();

        Cliente cliente = new Cliente();
        cliente.setId(UUID.randomUUID());
        cliente.setNome("Advocacia Modelo Cliente");

        Processo processo = new Processo();
        processo.setId(processoId);
        processo.setNumeroCnj("5000001-12.2026.8.21.0001");
        processo.setCliente(cliente);

        when(clienteRepository.findAll()).thenReturn(Collections.singletonList(cliente));
        when(processoRepository.countByAdvogadoIdAndFaseAtualNot(usuarioId, FaseProcessualEnum.ARQUIVADO)).thenReturn(1);

        Tarefa tarefa = new Tarefa();
        tarefa.setId(UUID.randomUUID());
        tarefa.setDescricao("Revisar minuta");
        tarefa.setDataVencimento(LocalDate.now());
        tarefa.setProcesso(processo);
        when(tarefaRepository.findByUsuarioIdAndConcluidaFalseOrderByDataVencimentoAsc(usuarioId))
                .thenReturn(Collections.singletonList(tarefa));

        Faturamento faturamento = new Faturamento();
        faturamento.setId(UUID.randomUUID());
        faturamento.setDescricao("Honorários Contratuais");
        faturamento.setValor(new BigDecimal("1500.00"));
        faturamento.setDataVencimento(LocalDate.now());
        faturamento.setProcesso(processo);
        when(faturamentoRepository.findByStatusAndNaturezaOrderByDataVencimentoAsc(
                StatusFaturamentoEnum.PENDENTE,
                NaturezaFaturamentoEnum.A_RECEBER
        )).thenReturn(Collections.singletonList(faturamento));

        Audiencia audiencia = new Audiencia();
        audiencia.setId(UUID.randomUUID());
        audiencia.setDataHora(LocalDateTime.now().plusDays(1));
        audiencia.setLocal("Sala Virtual");
        audiencia.setStatus(StatusAudienciaEnum.AGENDADA);
        audiencia.setProcesso(processo);

        when(audienciaRepository.findByDataHoraBetweenOrderByDataHoraAsc(any(), any()))
                .thenReturn(Collections.singletonList(audiencia));

        ResumoDashboardDTO resultado = dashboardAdvogadoUseCase.executar(usuarioId);

        assertNotNull(resultado);
        assertEquals(1, resultado.totalClientesAtivos());
        assertEquals(1, resultado.totalProcessosAndamento());
        assertEquals(1, resultado.tarefasPendentesHoje());
        assertEquals(new BigDecimal("1500.00"), resultado.totalReceberHoje());

        // Verificação das faturas com ProcessoResumoDTO
        assertEquals(1, resultado.proximasFaturasReceber().size());
        assertNotNull(resultado.proximasFaturasReceber().get(0).processo());
        assertEquals(processoId, resultado.proximasFaturasReceber().get(0).processo().id());
        assertEquals("5000001-12.2026.8.21.0001", resultado.proximasFaturasReceber().get(0).processo().numeroCnj());
        assertEquals("Advocacia Modelo Cliente", resultado.proximasFaturasReceber().get(0).processo().nomeCliente());

        // Verificação das audiências com ProcessoResumoDTO
        assertEquals(1, resultado.proximasAudiencias().size());
        assertNotNull(resultado.proximasAudiencias().get(0).processo());
        assertEquals(processoId, resultado.proximasAudiencias().get(0).processo().id());
        assertEquals("5000001-12.2026.8.21.0001", resultado.proximasAudiencias().get(0).processo().numeroCnj());
        assertEquals("Advocacia Modelo Cliente", resultado.proximasAudiencias().get(0).processo().nomeCliente());

        // Verificação das tarefas com ProcessoResumoDTO
        assertEquals(1, resultado.proximasTarefas().size());
        assertNotNull(resultado.proximasTarefas().get(0).processo());
        assertEquals(processoId, resultado.proximasTarefas().get(0).processo().id());
        assertEquals("5000001-12.2026.8.21.0001", resultado.proximasTarefas().get(0).processo().numeroCnj());
        assertEquals("Advocacia Modelo Cliente", resultado.proximasTarefas().get(0).processo().nomeCliente());
    }
}
