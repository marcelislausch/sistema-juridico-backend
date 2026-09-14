package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Cliente;
import com.sistemajuridico.backend.core.domain.Faturamento;
import com.sistemajuridico.backend.core.domain.Processo;
import com.sistemajuridico.backend.core.domain.enums.*;
import com.sistemajuridico.backend.core.domain.exceptions.RecursoNaoEncontradoException;
import com.sistemajuridico.backend.core.domain.exceptions.RegraNegocioException;
import com.sistemajuridico.backend.infrastructure.persistence.ClienteRepository;
import com.sistemajuridico.backend.infrastructure.persistence.FaturamentoRepository;
import com.sistemajuridico.backend.infrastructure.persistence.ProcessoRepository;
import com.sistemajuridico.backend.presentation.dtos.ConsultaAvulsaDTO;
import com.sistemajuridico.backend.presentation.dtos.EditarFaturamentoDTO;
import com.sistemajuridico.backend.presentation.dtos.FaturamentoDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FaturamentoUseCasesTest {

    @Mock
    private FaturamentoRepository faturamentoRepository;

    @Mock
    private ProcessoRepository processoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    private GerarParcelamentoUseCase gerarParcelamentoUseCase;
    private LiquidarFaturamentoUseCase liquidarFaturamentoUseCase;
    private LiquidarParcialFaturamentoUseCase liquidarParcialFaturamentoUseCase;
    private RepassarFaturamentoUseCase repassarFaturamentoUseCase;
    private RegistrarConsultaAvulsaUseCase registrarConsultaAvulsaUseCase;
    private EditarFaturamentoUseCase editarFaturamentoUseCase;

    @BeforeEach
    void setUp() {
        this.gerarParcelamentoUseCase = new GerarParcelamentoUseCase(faturamentoRepository, processoRepository);
        this.liquidarFaturamentoUseCase = new LiquidarFaturamentoUseCase(faturamentoRepository);
        this.liquidarParcialFaturamentoUseCase = new LiquidarParcialFaturamentoUseCase(faturamentoRepository);
        this.repassarFaturamentoUseCase = new RepassarFaturamentoUseCase(faturamentoRepository);
        this.registrarConsultaAvulsaUseCase = new RegistrarConsultaAvulsaUseCase(faturamentoRepository, clienteRepository);
        this.editarFaturamentoUseCase = new EditarFaturamentoUseCase(faturamentoRepository, processoRepository);
    }

    @Test
    void shouldGerarParcelamentoWithSuccess() {
        UUID processoId = UUID.randomUUID();
        Processo processo = new Processo();
        processo.setId(processoId);

        when(processoRepository.findById(processoId)).thenReturn(Optional.of(processo));
        when(faturamentoRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        List<FaturamentoDTO> dtos = new ArrayList<>();
        dtos.add(new FaturamentoDTO(
                null, "Parcela 1/2", new BigDecimal("1000.00"),
                TipoFaturamentoEnum.HONORARIOS, StatusFaturamentoEnum.PENDENTE, NaturezaFaturamentoEnum.A_RECEBER,
                LocalDate.now().plusDays(30), null, processoId,
                1, 2, OrigemPagamentoEnum.DIRETO_CLIENTE, null, null, null, null, null, null
        ));
        dtos.add(new FaturamentoDTO(
                null, "Parcela 2/2", new BigDecimal("1000.00"),
                TipoFaturamentoEnum.HONORARIOS, StatusFaturamentoEnum.PENDENTE, NaturezaFaturamentoEnum.A_RECEBER,
                LocalDate.now().plusDays(60), null, processoId,
                2, 2, OrigemPagamentoEnum.DIRETO_CLIENTE, null, null, null, null, null, null
        ));

        List<Faturamento> resultado = gerarParcelamentoUseCase.executar(dtos);

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Parcela 1/2", resultado.get(0).getDescricao());
        assertEquals(Integer.valueOf(1), resultado.get(0).getNumeroParcela());
        assertEquals(processo, resultado.get(0).getProcesso());
        assertEquals("Parcela 2/2", resultado.get(1).getDescricao());
        assertEquals(Integer.valueOf(2), resultado.get(1).getNumeroParcela());
        verify(faturamentoRepository, times(1)).saveAll(any());
    }

    @Test
    void shouldThrowWhenGerarParcelamentoEmpty() {
        assertThrows(RegraNegocioException.class, () -> gerarParcelamentoUseCase.executar(null));
        assertThrows(RegraNegocioException.class, () -> gerarParcelamentoUseCase.executar(new ArrayList<>()));
    }

    @Test
    void shouldLiquidarIntegralmenteComDataAtualSeNula() {
        UUID id = UUID.randomUUID();
        Faturamento faturamento = new Faturamento();
        faturamento.setId(id);
        faturamento.setStatus(StatusFaturamentoEnum.PENDENTE);

        when(faturamentoRepository.findById(id)).thenReturn(Optional.of(faturamento));
        when(faturamentoRepository.save(any(Faturamento.class))).thenAnswer(i -> i.getArgument(0));

        Faturamento liquidado = liquidarFaturamentoUseCase.executar(id, null);

        assertNotNull(liquidado);
        assertEquals(StatusFaturamentoEnum.PAGO, liquidado.getStatus());
        assertEquals(LocalDate.now(), liquidado.getDataPagamento());
    }

    @Test
    void shouldLiquidarIntegralmenteComDataInformada() {
        UUID id = UUID.randomUUID();
        Faturamento faturamento = new Faturamento();
        faturamento.setId(id);
        faturamento.setStatus(StatusFaturamentoEnum.PENDENTE);
        LocalDate dataEfetiva = LocalDate.of(2026, 9, 8);

        when(faturamentoRepository.findById(id)).thenReturn(Optional.of(faturamento));
        when(faturamentoRepository.save(any(Faturamento.class))).thenAnswer(i -> i.getArgument(0));

        Faturamento liquidado = liquidarFaturamentoUseCase.executar(id, dataEfetiva);

        assertNotNull(liquidado);
        assertEquals(StatusFaturamentoEnum.PAGO, liquidado.getStatus());
        assertEquals(dataEfetiva, liquidado.getDataPagamento());
    }

    @Test
    void shouldLiquidarParcialmenteEDesdobrarSaldo() {
        UUID id = UUID.randomUUID();
        Faturamento original = new Faturamento();
        original.setId(id);
        original.setDescricao("Honorários Iniciais");
        original.setValor(new BigDecimal("3000.00"));
        original.setTipo(TipoFaturamentoEnum.HONORARIOS);
        original.setNatureza(NaturezaFaturamentoEnum.A_RECEBER);
        original.setStatus(StatusFaturamentoEnum.PENDENTE);
        original.setDataVencimento(LocalDate.of(2026, 9, 15));

        when(faturamentoRepository.findById(id)).thenReturn(Optional.of(original));
        when(faturamentoRepository.save(any(Faturamento.class))).thenAnswer(i -> i.getArgument(0));

        BigDecimal valorPago = new BigDecimal("1000.00");
        LocalDate novaDataVencimento = LocalDate.of(2026, 10, 15);
        LocalDate dataPagamento = LocalDate.of(2026, 9, 10);

        Faturamento originalAtualizado = liquidarParcialFaturamentoUseCase.executar(
                id, valorPago, novaDataVencimento, dataPagamento
        );

        // Verifica o original
        assertNotNull(originalAtualizado);
        assertEquals(new BigDecimal("1000.00"), originalAtualizado.getValor());
        assertEquals(StatusFaturamentoEnum.PAGO, originalAtualizado.getStatus());
        assertEquals(dataPagamento, originalAtualizado.getDataPagamento());

        // Captura o clone salvo
        ArgumentCaptor<Faturamento> captor = ArgumentCaptor.forClass(Faturamento.class);
        verify(faturamentoRepository, times(2)).save(captor.capture());

        List<Faturamento> salvos = captor.getAllValues();
        Faturamento desdobrado = salvos.get(0);

        assertEquals(new BigDecimal("2000.00"), desdobrado.getValor());
        assertEquals(StatusFaturamentoEnum.PENDENTE, desdobrado.getStatus());
        assertEquals(novaDataVencimento, desdobrado.getDataVencimento());
        assertNull(desdobrado.getDataPagamento());
        assertTrue(desdobrado.getDescricao().contains("Saldo Remanescente"));
    }

    @Test
    void shouldThrowWhenLiquidarParcialComValorInvalido() {
        UUID id = UUID.randomUUID();
        Faturamento original = new Faturamento();
        original.setId(id);
        original.setValor(new BigDecimal("1000.00"));
        original.setStatus(StatusFaturamentoEnum.PENDENTE);

        when(faturamentoRepository.findById(id)).thenReturn(Optional.of(original));

        // Valor <= 0
        assertThrows(RegraNegocioException.class, () ->
                liquidarParcialFaturamentoUseCase.executar(id, BigDecimal.ZERO, LocalDate.now().plusDays(10), null)
        );

        // Valor >= total
        assertThrows(RegraNegocioException.class, () ->
                liquidarParcialFaturamentoUseCase.executar(id, new BigDecimal("1000.00"), LocalDate.now().plusDays(10), null)
        );

        // Nova data nula
        assertThrows(RegraNegocioException.class, () ->
                liquidarParcialFaturamentoUseCase.executar(id, new BigDecimal("500.00"), null, null)
        );
    }

    @Test
    void shouldRepassarFaturamentoComSucesso() {
        UUID id = UUID.randomUUID();
        Faturamento faturamento = new Faturamento();
        faturamento.setId(id);
        faturamento.setStatus(StatusFaturamentoEnum.PAGO);
        faturamento.setOrigemPagamento(OrigemPagamentoEnum.TERCEIRO_SUCUMBENCIA);
        faturamento.setStatusRepasse(StatusRepasseEnum.PENDENTE);

        when(faturamentoRepository.findById(id)).thenReturn(Optional.of(faturamento));
        when(faturamentoRepository.save(any(Faturamento.class))).thenAnswer(i -> i.getArgument(0));

        Faturamento repassado = repassarFaturamentoUseCase.executar(id, LocalDate.now(), "PIX");

        assertNotNull(repassado);
        assertEquals(StatusRepasseEnum.REPASSADO, repassado.getStatusRepasse());
        assertEquals(LocalDate.now(), repassado.getDataRepasse());
        assertEquals("PIX", repassado.getFormaRepasse());
    }

    @Test
    void shouldThrowRepassarQuandoOrigemNaoForSucumbencia() {
        UUID id = UUID.randomUUID();
        Faturamento faturamento = new Faturamento();
        faturamento.setId(id);
        faturamento.setOrigemPagamento(OrigemPagamentoEnum.DIRETO_CLIENTE);

        when(faturamentoRepository.findById(id)).thenReturn(Optional.of(faturamento));

        assertThrows(RegraNegocioException.class, () ->
                repassarFaturamentoUseCase.executar(id, null, null)
        );
    }

    @Test
    void shouldThrowQuandoFaturamentoNaoEncontrado() {
        UUID id = UUID.randomUUID();
        when(faturamentoRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () ->
                liquidarFaturamentoUseCase.executar(id, null)
        );
        assertThrows(RecursoNaoEncontradoException.class, () ->
                liquidarParcialFaturamentoUseCase.executar(id, new BigDecimal("100.00"), LocalDate.now(), null)
        );
        assertThrows(RecursoNaoEncontradoException.class, () ->
                repassarFaturamentoUseCase.executar(id, null, null)
        );
    }

    @Test
    void shouldRegistrarConsultaAvulsaComSucessoSemVinculoProcessual() {
        UUID clienteId = UUID.randomUUID();
        Cliente cliente = new Cliente();
        cliente.setId(clienteId);
        cliente.setNome("João da Silva");

        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
        when(faturamentoRepository.save(any(Faturamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ConsultaAvulsaDTO dto = new ConsultaAvulsaDTO(
                clienteId,
                new BigDecimal("250.00"),
                "Consulta inicial sobre rescisão trabalhista",
                null,
                "PIX"
        );

        Faturamento resultado = registrarConsultaAvulsaUseCase.executar(dto);

        assertNotNull(resultado);
        assertEquals(cliente, resultado.getCliente());
        assertNull(resultado.getProcesso(), "O faturamento de consulta avulsa não deve ter processo vinculado");
        assertEquals(new BigDecimal("250.00"), resultado.getValor());
        assertEquals("Consulta inicial sobre rescisão trabalhista", resultado.getDescricao());
        assertEquals(TipoFaturamentoEnum.CONSULTA_AVULSA, resultado.getTipo());
        assertEquals(NaturezaFaturamentoEnum.A_RECEBER, resultado.getNatureza());
        assertEquals(StatusFaturamentoEnum.PAGO, resultado.getStatus(), "Status deve ser PAGO imediatamente na criação");
        assertEquals(LocalDate.now(), resultado.getDataPagamento());
        assertEquals(LocalDate.now(), resultado.getDataVencimento());
        assertEquals(Integer.valueOf(1), resultado.getNumeroParcela());
        assertEquals(Integer.valueOf(1), resultado.getTotalParcelas());
        assertEquals("PIX", resultado.getFormaRepasse());

        verify(clienteRepository, times(1)).findById(clienteId);
        verify(faturamentoRepository, times(1)).save(any(Faturamento.class));
    }

    @Test
    void shouldRegistrarConsultaAvulsaComDataInformada() {
        UUID clienteId = UUID.randomUUID();
        Cliente cliente = new Cliente();
        cliente.setId(clienteId);

        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
        when(faturamentoRepository.save(any(Faturamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LocalDate dataEspecifica = LocalDate.of(2026, 9, 10);
        ConsultaAvulsaDTO dto = new ConsultaAvulsaDTO(
                clienteId,
                new BigDecimal("500.00"),
                "Parecer previdenciário",
                dataEspecifica,
                "Cartão de Débito"
        );

        Faturamento resultado = registrarConsultaAvulsaUseCase.executar(dto);

        assertNotNull(resultado);
        assertEquals(StatusFaturamentoEnum.PAGO, resultado.getStatus());
        assertEquals(dataEspecifica, resultado.getDataPagamento());
        assertEquals(dataEspecifica, resultado.getDataVencimento());
        assertNull(resultado.getProcesso());
        assertEquals(cliente, resultado.getCliente());
    }

    @Test
    void shouldThrowWhenClienteNaoEncontradoAoRegistrarConsultaAvulsa() {
        UUID clienteId = UUID.randomUUID();
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.empty());

        ConsultaAvulsaDTO dto = new ConsultaAvulsaDTO(
                clienteId,
                new BigDecimal("250.00"),
                "Consulta não realizada",
                null,
                null
        );

        assertThrows(RecursoNaoEncontradoException.class, () ->
                registrarConsultaAvulsaUseCase.executar(dto)
        );
        verify(faturamentoRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenDadosInvalidosAoRegistrarConsultaAvulsa() {
        assertThrows(RegraNegocioException.class, () ->
                registrarConsultaAvulsaUseCase.executar(null)
        );
        assertThrows(RegraNegocioException.class, () ->
                registrarConsultaAvulsaUseCase.executar(new ConsultaAvulsaDTO(null, new BigDecimal("250.00"), "Consulta"))
        );
        assertThrows(RegraNegocioException.class, () ->
                registrarConsultaAvulsaUseCase.executar(new ConsultaAvulsaDTO(UUID.randomUUID(), null, "Consulta"))
        );
        assertThrows(RegraNegocioException.class, () ->
                registrarConsultaAvulsaUseCase.executar(new ConsultaAvulsaDTO(UUID.randomUUID(), BigDecimal.ZERO, "Consulta"))
        );
        assertThrows(RegraNegocioException.class, () ->
                registrarConsultaAvulsaUseCase.executar(new ConsultaAvulsaDTO(UUID.randomUUID(), new BigDecimal("-50.00"), "Consulta"))
        );
        assertThrows(RegraNegocioException.class, () ->
                registrarConsultaAvulsaUseCase.executar(new ConsultaAvulsaDTO(UUID.randomUUID(), new BigDecimal("250.00"), ""))
        );
        assertThrows(RegraNegocioException.class, () ->
                registrarConsultaAvulsaUseCase.executar(new ConsultaAvulsaDTO(UUID.randomUUID(), new BigDecimal("250.00"), "   "))
        );
        verify(faturamentoRepository, never()).save(any());
    }

    @Test
    void shouldEditarFaturamentoComSucesso() {
        UUID faturamentoId = UUID.randomUUID();
        Faturamento faturamento = new Faturamento();
        faturamento.setId(faturamentoId);
        faturamento.setDescricao("Valor Antigo");
        faturamento.setValor(new BigDecimal("100.00"));
        faturamento.setTipo(TipoFaturamentoEnum.CUSTAS);
        faturamento.setStatus(StatusFaturamentoEnum.PENDENTE);
        faturamento.setDataVencimento(LocalDate.now().plusDays(5));

        when(faturamentoRepository.findById(faturamentoId)).thenReturn(Optional.of(faturamento));
        when(faturamentoRepository.save(any(Faturamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EditarFaturamentoDTO dto = new EditarFaturamentoDTO(
                "Valor Corrigido",
                new BigDecimal("500.00"),
                TipoFaturamentoEnum.HONORARIOS,
                StatusFaturamentoEnum.PAGO,
                NaturezaFaturamentoEnum.A_RECEBER,
                LocalDate.now().plusDays(10),
                LocalDate.now(),
                null
        );

        Faturamento resultado = editarFaturamentoUseCase.executar(faturamentoId, dto);

        assertNotNull(resultado);
        assertEquals("Valor Corrigido", resultado.getDescricao());
        assertEquals(new BigDecimal("500.00"), resultado.getValor());
        assertEquals(TipoFaturamentoEnum.HONORARIOS, resultado.getTipo());
        assertEquals(StatusFaturamentoEnum.PAGO, resultado.getStatus());
        assertEquals(LocalDate.now().plusDays(10), resultado.getDataVencimento());
        verify(faturamentoRepository, times(1)).save(faturamento);
    }

    @Test
    void shouldThrowWhenFaturamentoNaoEncontradoAoEditar() {
        UUID faturamentoId = UUID.randomUUID();
        when(faturamentoRepository.findById(faturamentoId)).thenReturn(Optional.empty());

        EditarFaturamentoDTO dto = new EditarFaturamentoDTO(
                "Teste",
                new BigDecimal("100.00"),
                null, null, null, null, null, null
        );

        assertThrows(RecursoNaoEncontradoException.class, () ->
                editarFaturamentoUseCase.executar(faturamentoId, dto)
        );
    }

    @Test
    void shouldThrowWhenValorInvalidoAoEditar() {
        UUID faturamentoId = UUID.randomUUID();
        Faturamento faturamento = new Faturamento();
        faturamento.setId(faturamentoId);

        when(faturamentoRepository.findById(faturamentoId)).thenReturn(Optional.of(faturamento));

        EditarFaturamentoDTO dtoValorZero = new EditarFaturamentoDTO(
                "Teste",
                BigDecimal.ZERO,
                null, null, null, null, null, null
        );

        assertThrows(RegraNegocioException.class, () ->
                editarFaturamentoUseCase.executar(faturamentoId, dtoValorZero)
        );
    }
}
