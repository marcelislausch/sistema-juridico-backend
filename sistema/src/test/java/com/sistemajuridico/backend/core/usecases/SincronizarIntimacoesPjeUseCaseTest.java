package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Andamento;
import com.sistemajuridico.backend.core.domain.IntimacaoPje;
import com.sistemajuridico.backend.core.domain.Processo;
import com.sistemajuridico.backend.core.domain.Tarefa;
import com.sistemajuridico.backend.core.domain.Usuario;
import com.sistemajuridico.backend.core.domain.enums.PerfilAcessoEnum;
import com.sistemajuridico.backend.core.domain.enums.TipoTarefaEnum;
import com.sistemajuridico.backend.core.domain.exceptions.RegraNegocioException;
import com.sistemajuridico.backend.infrastructure.integrations.pje.ComunicaPjeClient;
import com.sistemajuridico.backend.infrastructure.persistence.AndamentoRepository;
import com.sistemajuridico.backend.infrastructure.persistence.IntimacaoPjeRepository;
import com.sistemajuridico.backend.infrastructure.persistence.ProcessoRepository;
import com.sistemajuridico.backend.infrastructure.persistence.TarefaRepository;
import com.sistemajuridico.backend.infrastructure.persistence.UsuarioRepository;
import com.sistemajuridico.backend.presentation.dtos.pje.ComunicaPjeItemDTO;
import com.sistemajuridico.backend.presentation.dtos.pje.ComunicaPjeResponseDTO;
import com.sistemajuridico.backend.presentation.dtos.pje.SincronizacaoPjeResultadoDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SincronizarIntimacoesPjeUseCaseTest {

    @Mock
    private ComunicaPjeClient comunicaPjeClient;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ProcessoRepository processoRepository;

    @Mock
    private AndamentoRepository andamentoRepository;

    @Mock
    private IntimacaoPjeRepository intimacaoPjeRepository;

    @Mock
    private TarefaRepository tarefaRepository;

    @InjectMocks
    private SincronizarIntimacoesPjeUseCase sincronizarIntimacoesPjeUseCase;

    private Usuario advogado;

    @BeforeEach
    void setUp() {
        this.advogado = new Usuario();
        this.advogado.setId(UUID.randomUUID());
        this.advogado.setNome("Dr. Cristhian Menezes");
        this.advogado.setEmail("cristhian@escritorio.com");
        this.advogado.setPerfil(PerfilAcessoEnum.ADVOGADO);
        this.advogado.setOab("RS121837");
    }

    @Test
    void deveSincronizarIntimacoesComSucessoEVincularAoProcessoExistente() {
        LocalDate dataInicio = LocalDate.of(2026, 9, 10);
        LocalDate dataFim = LocalDate.of(2026, 9, 14);

        ComunicaPjeItemDTO item = new ComunicaPjeItemDTO(
                999888777L,
                "2026-09-14",
                "TJRS",
                "Intimação",
                "1ª Vara Cível de Ijuí",
                1234,
                "Fica intimado o autor para...",
                "50012344520268210016",
                "5001234-45.2026.8.21.0016",
                "D",
                "https://pje.tjrs.jus.br",
                "Despacho",
                "Procedimento Comum",
                "7",
                "hash123",
                null
        );

        List<ComunicaPjeItemDTO> itens = new ArrayList<>();
        itens.add(item);

        ComunicaPjeResponseDTO response = new ComunicaPjeResponseDTO(
                "success",
                "Sucesso",
                1,
                itens
        );

        when(comunicaPjeClient.consultar("121837", "RS", dataInicio, dataFim, 1, 50))
                .thenReturn(response);

        when(intimacaoPjeRepository.existsByComunicacaoId(999888777L)).thenReturn(false);

        Processo processo = new Processo();
        processo.setId(UUID.randomUUID());
        processo.setNumeroCnj("5001234-45.2026.8.21.0016");

        when(processoRepository.findByNumeroCnjExato("5001234-45.2026.8.21.0016"))
                .thenReturn(Optional.of(processo));

        SincronizacaoPjeResultadoDTO resultado = sincronizarIntimacoesPjeUseCase.executarParaAdvogado(
                this.advogado, dataInicio, dataFim
        );

        assertNotNull(resultado);
        assertEquals(1, resultado.totalEncontradas());
        assertEquals(1, resultado.novasIntimacoes());
        assertEquals(1, resultado.andamentosCriados());
        assertEquals("121837", resultado.numeroOabConsultada());
        assertEquals("RS", resultado.ufOabConsultada());

        verify(intimacaoPjeRepository, times(1)).save(any(IntimacaoPje.class));

        ArgumentCaptor<Andamento> captorAndamento = ArgumentCaptor.forClass(Andamento.class);
        verify(andamentoRepository, times(1)).save(captorAndamento.capture());
        Andamento andamentoSalvo = captorAndamento.getValue();
        assertEquals("[PJe - TJRS] Intimação (Despacho)\nÓrgão: 1ª Vara Cível de Ijuí\n\nFica intimado o autor para...", andamentoSalvo.getDescricao());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Tarefa>> captorTarefas = ArgumentCaptor.forClass(List.class);
        verify(tarefaRepository, times(1)).saveAll(captorTarefas.capture());

        List<Tarefa> tarefasSalvas = captorTarefas.getValue();
        assertNotNull(tarefasSalvas);
        assertEquals(1, tarefasSalvas.size());

        Tarefa tarefaCriada = tarefasSalvas.get(0);
        assertEquals("[URGENTE - DECISÃO] Proc. 50012344520268210016 (TJRS)", tarefaCriada.getDescricao());
        assertEquals(TipoTarefaEnum.PRAZO, tarefaCriada.getTipo());
        assertEquals(LocalDate.of(2026, 9, 14), tarefaCriada.getDataVencimento());
        assertEquals(this.advogado, tarefaCriada.getUsuario());
        assertEquals(processo, tarefaCriada.getProcesso());
        assertFalse(tarefaCriada.getConcluida());
    }

    @Test
    void deveIgnorarIntimacaoQuandoJaExistirNoBanco() {
        LocalDate dataInicio = LocalDate.of(2026, 9, 10);
        LocalDate dataFim = LocalDate.of(2026, 9, 14);

        ComunicaPjeItemDTO item = new ComunicaPjeItemDTO(
                111222333L,
                "2026-09-14",
                "TRF4",
                "Edital",
                "Vara Federal",
                100,
                "Texto do edital",
                "50099990020264047100",
                "5009999-00.2026.4.04.7100",
                "E",
                null,
                "Edital",
                "Execução Fiscal",
                "12",
                "hash456",
                null
        );

        List<ComunicaPjeItemDTO> itens = new ArrayList<>();
        itens.add(item);

        ComunicaPjeResponseDTO response = new ComunicaPjeResponseDTO("success", "Sucesso", 1, itens);

        when(comunicaPjeClient.consultar("121837", "RS", dataInicio, dataFim, 1, 50))
                .thenReturn(response);

        when(intimacaoPjeRepository.existsByComunicacaoId(111222333L)).thenReturn(true);

        SincronizacaoPjeResultadoDTO resultado = sincronizarIntimacoesPjeUseCase.executarParaAdvogado(
                this.advogado, dataInicio, dataFim
        );

        assertNotNull(resultado);
        assertEquals(1, resultado.totalEncontradas());
        assertEquals(0, resultado.novasIntimacoes());
        assertEquals(0, resultado.andamentosCriados());

        verify(intimacaoPjeRepository, never()).save(any(IntimacaoPje.class));
        verify(andamentoRepository, never()).save(any(Andamento.class));
        verify(tarefaRepository, never()).saveAll(any());
    }

    @Test
    void deveCriarTarefaMesmoQuandoProcessoNaoForLocalizadoNoBanco() {
        LocalDate dataInicio = LocalDate.of(2026, 9, 10);
        LocalDate dataFim = LocalDate.of(2026, 9, 14);

        ComunicaPjeItemDTO item = new ComunicaPjeItemDTO(
                555666777L,
                "2026-09-14",
                "TJRS",
                "Intimação",
                "2ª Vara Cível de Porto Alegre",
                10,
                "Intimação sem processo cadastrado",
                "50088881120268210001",
                "5008888-11.2026.8.21.0001",
                "D",
                null,
                "Despacho",
                "Procedimento Comum",
                "1",
                "hash789",
                null
        );

        List<ComunicaPjeItemDTO> itens = new ArrayList<>();
        itens.add(item);

        ComunicaPjeResponseDTO response = new ComunicaPjeResponseDTO("success", "Sucesso", 1, itens);

        when(comunicaPjeClient.consultar("121837", "RS", dataInicio, dataFim, 1, 50))
                .thenReturn(response);

        when(intimacaoPjeRepository.existsByComunicacaoId(555666777L)).thenReturn(false);
        when(processoRepository.findByNumeroCnjExato("5008888-11.2026.8.21.0001")).thenReturn(Optional.empty());
        when(processoRepository.findByNumeroCnjLimpo("50088881120268210001")).thenReturn(Optional.empty());

        SincronizacaoPjeResultadoDTO resultado = sincronizarIntimacoesPjeUseCase.executarParaAdvogado(
                this.advogado, dataInicio, dataFim
        );

        assertNotNull(resultado);
        assertEquals(1, resultado.novasIntimacoes());
        assertEquals(0, resultado.andamentosCriados());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Tarefa>> captorTarefas = ArgumentCaptor.forClass(List.class);
        verify(tarefaRepository, times(1)).saveAll(captorTarefas.capture());

        List<Tarefa> tarefasSalvas = captorTarefas.getValue();
        assertEquals(1, tarefasSalvas.size());
        Tarefa tarefaCriada = tarefasSalvas.get(0);
        assertEquals("[URGENTE - DECISÃO] Proc. 50088881120268210001 (TJRS)", tarefaCriada.getDescricao());
        assertEquals(TipoTarefaEnum.PRAZO, tarefaCriada.getTipo());
        assertNull(tarefaCriada.getProcesso());
        assertEquals(this.advogado, tarefaCriada.getUsuario());
    }

    @Test
    void deveLancarRegraNegocioExceptionQuandoAdvogadoNaoPossuirOab() {
        this.advogado.setOab(null);

        assertThrows(RegraNegocioException.class, () -> {
            sincronizarIntimacoesPjeUseCase.executarParaAdvogado(this.advogado, LocalDate.now(), LocalDate.now());
        });
    }

    @Test
    void deveExecutarVarreduraGeralParaTodosAdvogadosComOabAtiva() {
        Usuario adv2 = new Usuario();
        adv2.setId(UUID.randomUUID());
        adv2.setNome("Dra. Marceli Lausch");
        adv2.setOab("RS123456");

        List<Usuario> advogados = new ArrayList<>();
        advogados.add(this.advogado);
        advogados.add(adv2);

        when(usuarioRepository.buscarAdvogadosComOabAtiva()).thenReturn(advogados);

        when(comunicaPjeClient.consultar(anyString(), anyString(), any(), any(), eq(1), eq(50)))
                .thenReturn(new ComunicaPjeResponseDTO("success", "OK", 0, new ArrayList<>()));

        sincronizarIntimacoesPjeUseCase.executarVarreduraGeral(LocalDate.now(), LocalDate.now());

        verify(usuarioRepository, times(1)).buscarAdvogadosComOabAtiva();
        verify(comunicaPjeClient, times(2)).consultar(anyString(), anyString(), any(), any(), eq(1), eq(50));
        verify(comunicaPjeClient, times(1)).executarDelayDefensivo(3500L);
    }

    @Test
    void deveDescartarCriacaoDeTarefaQuandoTipoComunicacaoForListaDeDistribuicaoOuAtaDeSessao() {
        LocalDate dataInicio = LocalDate.of(2026, 9, 10);
        LocalDate dataFim = LocalDate.of(2026, 9, 14);

        ComunicaPjeItemDTO itemLista = new ComunicaPjeItemDTO(
                1001L, "2026-09-14", "TJRS", "Lista de distribuição", "Distribuidor",
                1, "Distribuição", "50011112220268210001", "5001111-22.2026.8.21.0001",
                "D", null, "Outros", "Procedimento", "1", "hash1", null
        );

        ComunicaPjeItemDTO itemAta = new ComunicaPjeItemDTO(
                1002L, "2026-09-14", "TRF4", "Ata de sessão", "Plenário",
                2, "Ata", "50022223320264047100", "5002222-33.2026.4.04.7100",
                "D", null, "Outros", "Apelação", "2", "hash2", null
        );

        List<ComunicaPjeItemDTO> itens = new ArrayList<>();
        itens.add(itemLista);
        itens.add(itemAta);

        ComunicaPjeResponseDTO response = new ComunicaPjeResponseDTO("success", "OK", 2, itens);

        when(comunicaPjeClient.consultar("121837", "RS", dataInicio, dataFim, 1, 50))
                .thenReturn(response);

        when(intimacaoPjeRepository.existsByComunicacaoId(1001L)).thenReturn(false);
        when(intimacaoPjeRepository.existsByComunicacaoId(1002L)).thenReturn(false);

        SincronizacaoPjeResultadoDTO resultado = sincronizarIntimacoesPjeUseCase.executarParaAdvogado(
                this.advogado, dataInicio, dataFim
        );

        assertNotNull(resultado);
        assertEquals(2, resultado.novasIntimacoes());
        verify(intimacaoPjeRepository, times(2)).save(any(IntimacaoPje.class));
        verify(tarefaRepository, never()).saveAll(any());
    }

    @Test
    void deveCriarTarefaDiligenciaQuandoTipoComunicacaoForPautaDeJulgamento() {
        LocalDate dataInicio = LocalDate.of(2026, 9, 10);
        LocalDate dataFim = LocalDate.of(2026, 9, 14);

        ComunicaPjeItemDTO itemPauta = new ComunicaPjeItemDTO(
                2001L, "2026-09-14", "TJRS", "Pauta de julgamento", "1ª Câmara Cível",
                1, "Julgamento pautado", "50033334420268210001", "5003333-44.2026.8.21.0001",
                "D", null, "Certidão", "Apelação Cível", "1", "hashPauta", null
        );

        List<ComunicaPjeItemDTO> itens = new ArrayList<>();
        itens.add(itemPauta);

        when(comunicaPjeClient.consultar("121837", "RS", dataInicio, dataFim, 1, 50))
                .thenReturn(new ComunicaPjeResponseDTO("success", "OK", 1, itens));

        when(intimacaoPjeRepository.existsByComunicacaoId(2001L)).thenReturn(false);

        sincronizarIntimacoesPjeUseCase.executarParaAdvogado(this.advogado, dataInicio, dataFim);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Tarefa>> captorTarefas = ArgumentCaptor.forClass(List.class);
        verify(tarefaRepository, times(1)).saveAll(captorTarefas.capture());

        List<Tarefa> tarefas = captorTarefas.getValue();
        assertEquals(1, tarefas.size());
        assertEquals(TipoTarefaEnum.DILIGENCIA, tarefas.get(0).getTipo());
        assertEquals("[DILIGÊNCIA - PAUTA] Proc. 50033334420268210001 (TJRS)", tarefas.get(0).getDescricao());
    }

    @Test
    void deveCriarTarefaUrgenteSentencaQuandoTipoDocumentoForSentenca() {
        LocalDate dataInicio = LocalDate.of(2026, 9, 10);
        LocalDate dataFim = LocalDate.of(2026, 9, 14);

        ComunicaPjeItemDTO itemSentenca = new ComunicaPjeItemDTO(
                3001L, "2026-09-14", "TJRS", "Intimação", "Vara Cível",
                1, "Sentença procedente", "50044445520268210001", "5004444-55.2026.8.21.0001",
                "D", null, "Sentença", "Comum", "1", "hashSentenca", null
        );

        List<ComunicaPjeItemDTO> itens = new ArrayList<>();
        itens.add(itemSentenca);

        when(comunicaPjeClient.consultar("121837", "RS", dataInicio, dataFim, 1, 50))
                .thenReturn(new ComunicaPjeResponseDTO("success", "OK", 1, itens));
        when(intimacaoPjeRepository.existsByComunicacaoId(3001L)).thenReturn(false);

        sincronizarIntimacoesPjeUseCase.executarParaAdvogado(this.advogado, dataInicio, dataFim);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Tarefa>> captorTarefas = ArgumentCaptor.forClass(List.class);
        verify(tarefaRepository, times(1)).saveAll(captorTarefas.capture());

        List<Tarefa> tarefas = captorTarefas.getValue();
        assertEquals(TipoTarefaEnum.PRAZO, tarefas.get(0).getTipo());
        assertEquals("[URGENTE - SENTENÇA] Proc. 50044445520268210001 (TJRS)", tarefas.get(0).getDescricao());
    }

    @Test
    void deveCriarTarefaPrazoAtoOrdinatorioQuandoTipoDocumentoForAtoOrdinatorio() {
        LocalDate dataInicio = LocalDate.of(2026, 9, 10);
        LocalDate dataFim = LocalDate.of(2026, 9, 14);

        ComunicaPjeItemDTO itemAto = new ComunicaPjeItemDTO(
                4001L, "2026-09-14", "TJRS", "Intimação", "Vara Cível",
                1, "Ato ordinatório", "50055556620268210001", "5005555-66.2026.8.21.0001",
                "D", null, "Ato ordinatório", "Comum", "1", "hashAto", null
        );

        List<ComunicaPjeItemDTO> itens = new ArrayList<>();
        itens.add(itemAto);

        when(comunicaPjeClient.consultar("121837", "RS", dataInicio, dataFim, 1, 50))
                .thenReturn(new ComunicaPjeResponseDTO("success", "OK", 1, itens));
        when(intimacaoPjeRepository.existsByComunicacaoId(4001L)).thenReturn(false);

        sincronizarIntimacoesPjeUseCase.executarParaAdvogado(this.advogado, dataInicio, dataFim);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Tarefa>> captorTarefas = ArgumentCaptor.forClass(List.class);
        verify(tarefaRepository, times(1)).saveAll(captorTarefas.capture());

        List<Tarefa> tarefas = captorTarefas.getValue();
        assertEquals(TipoTarefaEnum.PRAZO, tarefas.get(0).getTipo());
        assertEquals("[PRAZO - ATO ORDINATÓRIO] Proc. 50055556620268210001 (TJRS)", tarefas.get(0).getDescricao());
    }

    @Test
    void deveCriarTarefaPrazoNotificacaoQuandoTipoDocumentoForNotificacaoECitacao() {
        LocalDate dataInicio = LocalDate.of(2026, 9, 10);
        LocalDate dataFim = LocalDate.of(2026, 9, 14);

        ComunicaPjeItemDTO itemNotificacao = new ComunicaPjeItemDTO(
                5001L, "2026-09-14", "TRF4", "Citação", "Vara Federal",
                1, "Notificação de citação", "50066667720264047100", "5006666-77.2026.4.04.7100",
                "C", null, "Notificação", "Execução Fiscal", "1", "hashNotif", null
        );

        List<ComunicaPjeItemDTO> itens = new ArrayList<>();
        itens.add(itemNotificacao);

        when(comunicaPjeClient.consultar("121837", "RS", dataInicio, dataFim, 1, 50))
                .thenReturn(new ComunicaPjeResponseDTO("success", "OK", 1, itens));
        when(intimacaoPjeRepository.existsByComunicacaoId(5001L)).thenReturn(false);

        sincronizarIntimacoesPjeUseCase.executarParaAdvogado(this.advogado, dataInicio, dataFim);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Tarefa>> captorTarefas = ArgumentCaptor.forClass(List.class);
        verify(tarefaRepository, times(1)).saveAll(captorTarefas.capture());

        List<Tarefa> tarefas = captorTarefas.getValue();
        assertEquals(TipoTarefaEnum.PRAZO, tarefas.get(0).getTipo());
        assertEquals("[PRAZO - NOTIFICAÇÃO] Proc. 50066667720264047100 (TRF4)", tarefas.get(0).getDescricao());
    }

    @Test
    void deveCriarTarefaPrazoAtencaoQuandoTipoDocumentoForOutrosOuDesconhecido() {
        LocalDate dataInicio = LocalDate.of(2026, 9, 10);
        LocalDate dataFim = LocalDate.of(2026, 9, 14);

        ComunicaPjeItemDTO itemOutros = new ComunicaPjeItemDTO(
                6001L, "2026-09-14", "TRT4", "Intimação", "Vara do Trabalho",
                1, "Outro documento", "00077778820265040001", "0007777-88.2026.5.04.0001",
                "D", null, "Outros", "Reclamatória", "1", "hashOutros", null
        );

        List<ComunicaPjeItemDTO> itens = new ArrayList<>();
        itens.add(itemOutros);

        when(comunicaPjeClient.consultar("121837", "RS", dataInicio, dataFim, 1, 50))
                .thenReturn(new ComunicaPjeResponseDTO("success", "OK", 1, itens));
        when(intimacaoPjeRepository.existsByComunicacaoId(6001L)).thenReturn(false);

        sincronizarIntimacoesPjeUseCase.executarParaAdvogado(this.advogado, dataInicio, dataFim);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Tarefa>> captorTarefas = ArgumentCaptor.forClass(List.class);
        verify(tarefaRepository, times(1)).saveAll(captorTarefas.capture());

        List<Tarefa> tarefas = captorTarefas.getValue();
        assertEquals(TipoTarefaEnum.PRAZO, tarefas.get(0).getTipo());
        assertEquals("[PRAZO - ATENÇÃO] Proc. 00077778820265040001 (TRT4)", tarefas.get(0).getDescricao());
    }

    @Test
    void deveSanitizarHtmlELimparEspacosAoCriarAndamento() {
        LocalDate dataInicio = LocalDate.of(2026, 9, 10);
        LocalDate dataFim = LocalDate.of(2026, 9, 14);

        String textoBruto = "<style type=\"text/css\">body { font-size: 10pt; color: #000; }</style>"
                + "<p>Fica intimada a parte autora&nbsp;para manifesta&ccedil;&atilde;o no prazo de 15&ordm; dia.<br/>"
                + "Apresente os c&aacute;lculos.<br><br><br><br>"
                + "Publique-se.\t\t\tIntime-se.</p>";

        ComunicaPjeItemDTO itemComHtml = new ComunicaPjeItemDTO(
                777888L,
                "2026-09-14",
                "TJRS",
                "Intimação",
                "Vara de Família",
                1,
                textoBruto,
                "50077771120268210001",
                "5007777-11.2026.8.21.0001",
                "D",
                null,
                "DESPACHO/DECISÃO",
                "Procedimento",
                "1",
                "hashHtml",
                null
        );

        List<ComunicaPjeItemDTO> itens = new ArrayList<>();
        itens.add(itemComHtml);

        when(comunicaPjeClient.consultar("121837", "RS", dataInicio, dataFim, 1, 50))
                .thenReturn(new ComunicaPjeResponseDTO("success", "OK", 1, itens));
        when(intimacaoPjeRepository.existsByComunicacaoId(777888L)).thenReturn(false);

        Processo proc = new Processo();
        proc.setId(UUID.randomUUID());
        proc.setNumeroCnj("5007777-11.2026.8.21.0001");
        when(processoRepository.findByNumeroCnjExato("5007777-11.2026.8.21.0001")).thenReturn(Optional.of(proc));

        sincronizarIntimacoesPjeUseCase.executarParaAdvogado(this.advogado, dataInicio, dataFim);

        ArgumentCaptor<Andamento> captorAndamento = ArgumentCaptor.forClass(Andamento.class);
        verify(andamentoRepository, times(1)).save(captorAndamento.capture());

        Andamento andamentoSalvo = captorAndamento.getValue();
        String esperada = "[PJe - TJRS] Intimação (DESPACHO/DECISÃO)\nÓrgão: Vara de Família\n\n"
                + "Fica intimada a parte autora para manifestação no prazo de 15º dia.\n"
                + "Apresente os cálculos.\n\n"
                + "Publique-se. Intime-se.";
        assertEquals(esperada, andamentoSalvo.getDescricao());
    }
}
