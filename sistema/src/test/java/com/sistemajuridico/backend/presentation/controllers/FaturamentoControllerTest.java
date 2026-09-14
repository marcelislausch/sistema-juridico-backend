package com.sistemajuridico.backend.presentation.controllers;

import com.sistemajuridico.backend.core.domain.Cliente;
import com.sistemajuridico.backend.core.domain.Faturamento;
import com.sistemajuridico.backend.core.domain.enums.NaturezaFaturamentoEnum;
import com.sistemajuridico.backend.core.domain.enums.StatusFaturamentoEnum;
import com.sistemajuridico.backend.core.domain.enums.TipoFaturamentoEnum;
import com.sistemajuridico.backend.core.usecases.*;
import com.sistemajuridico.backend.presentation.dtos.ConsultaAvulsaDTO;
import com.sistemajuridico.backend.presentation.dtos.EditarFaturamentoDTO;
import com.sistemajuridico.backend.presentation.dtos.FaturamentoDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FaturamentoControllerTest {

    @Mock
    private CadastrarFaturamentoUseCase cadastrarFaturamentoUseCase;
    @Mock
    private GerarParcelamentoUseCase gerarParcelamentoUseCase;
    @Mock
    private LiquidarFaturamentoUseCase liquidarFaturamentoUseCase;
    @Mock
    private LiquidarParcialFaturamentoUseCase liquidarParcialFaturamentoUseCase;
    @Mock
    private RepassarFaturamentoUseCase repassarFaturamentoUseCase;
    @Mock
    private ListarFaturamentosUseCase listarFaturamentosUseCase;
    @Mock
    private ObterResumoFinanceiroUseCase obterResumoFinanceiroUseCase;
    @Mock
    private RegistrarConsultaAvulsaUseCase registrarConsultaAvulsaUseCase;
    @Mock
    private EditarFaturamentoUseCase editarFaturamentoUseCase;

    private FaturamentoController controller;

    @BeforeEach
    void setUp() {
        controller = new FaturamentoController(
                cadastrarFaturamentoUseCase,
                editarFaturamentoUseCase,
                gerarParcelamentoUseCase,
                liquidarFaturamentoUseCase,
                liquidarParcialFaturamentoUseCase,
                repassarFaturamentoUseCase,
                listarFaturamentosUseCase,
                obterResumoFinanceiroUseCase,
                registrarConsultaAvulsaUseCase
        );
    }

    @Test
    void shouldRegistrarConsultaAvulsaWithStatusCreated() {
        UUID clienteId = UUID.randomUUID();
        ConsultaAvulsaDTO request = new ConsultaAvulsaDTO(clienteId, new BigDecimal("250.00"), "Consulta de balcão");

        Cliente cliente = new Cliente();
        cliente.setId(clienteId);
        cliente.setNome("Maria Silva");

        Faturamento faturamento = new Faturamento();
        faturamento.setId(UUID.randomUUID());
        faturamento.setDescricao("Consulta de balcão");
        faturamento.setValor(new BigDecimal("250.00"));
        faturamento.setTipo(TipoFaturamentoEnum.CONSULTA_AVULSA);
        faturamento.setNatureza(NaturezaFaturamentoEnum.A_RECEBER);
        faturamento.setStatus(StatusFaturamentoEnum.PAGO);
        faturamento.setDataVencimento(LocalDate.now());
        faturamento.setDataPagamento(LocalDate.now());
        faturamento.setCliente(cliente);
        faturamento.setProcesso(null);

        when(registrarConsultaAvulsaUseCase.executar(request)).thenReturn(faturamento);

        ResponseEntity<FaturamentoDTO> response = controller.registrarConsultaAvulsa(request);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(TipoFaturamentoEnum.CONSULTA_AVULSA, response.getBody().tipo());
        assertEquals(StatusFaturamentoEnum.PAGO, response.getBody().status());
        assertNull(response.getBody().processo());
        assertEquals(clienteId, response.getBody().cliente().id());
        verify(registrarConsultaAvulsaUseCase, times(1)).executar(request);
    }

    @Test
    void shouldEditarFaturamentoComSucesso() {
        UUID id = UUID.randomUUID();
        EditarFaturamentoDTO request = new EditarFaturamentoDTO(
                "Honorários Retificados",
                new BigDecimal("3500.00"),
                TipoFaturamentoEnum.HONORARIOS,
                StatusFaturamentoEnum.PENDENTE,
                NaturezaFaturamentoEnum.A_RECEBER,
                LocalDate.now().plusDays(15),
                null,
                null
        );

        Faturamento faturamento = new Faturamento();
        faturamento.setId(id);
        faturamento.setDescricao("Honorários Retificados");
        faturamento.setValor(new BigDecimal("3500.00"));
        faturamento.setTipo(TipoFaturamentoEnum.HONORARIOS);
        faturamento.setStatus(StatusFaturamentoEnum.PENDENTE);
        faturamento.setNatureza(NaturezaFaturamentoEnum.A_RECEBER);
        faturamento.setDataVencimento(LocalDate.now().plusDays(15));

        when(editarFaturamentoUseCase.executar(id, request)).thenReturn(faturamento);

        ResponseEntity<FaturamentoDTO> response = controller.editar(id, request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(id, response.getBody().id());
        assertEquals("Honorários Retificados", response.getBody().descricao());
        assertEquals(new BigDecimal("3500.00"), response.getBody().valor());
        verify(editarFaturamentoUseCase, times(1)).executar(id, request);
    }
}
