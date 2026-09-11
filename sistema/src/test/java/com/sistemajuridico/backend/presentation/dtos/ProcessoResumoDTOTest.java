package com.sistemajuridico.backend.presentation.dtos;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sistemajuridico.backend.core.domain.Cliente;
import com.sistemajuridico.backend.core.domain.Processo;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProcessoResumoDTOTest {

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void shouldMapFromEntityCorrectly() {
        UUID processoId = UUID.randomUUID();
        Processo processo = new Processo();
        processo.setId(processoId);
        processo.setNumeroCnj("5001234-56.2026.8.21.0016");

        Cliente cliente = new Cliente();
        cliente.setNome("Carlos Silva");
        processo.setCliente(cliente);

        ProcessoResumoDTO dto = ProcessoResumoDTO.fromEntity(processo);

        assertNotNull(dto);
        assertEquals(processoId, dto.id());
        assertEquals("5001234-56.2026.8.21.0016", dto.numeroCnj());
        assertEquals("Carlos Silva", dto.nomeCliente());
    }

    @Test
    void shouldSerializeAndDeserializeJson() throws Exception {
        UUID processoId = UUID.randomUUID();
        ProcessoResumoDTO dto = new ProcessoResumoDTO(processoId, "5001234-56.2026.8.21.0016", "Carlos Silva");

        String json = objectMapper.writeValueAsString(dto);
        assertTrue(json.contains("\"nomeCliente\":\"Carlos Silva\""));

        ProcessoResumoDTO deserialized = objectMapper.readValue(json, ProcessoResumoDTO.class);
        assertEquals(dto.id(), deserialized.id());
        assertEquals(dto.numeroCnj(), deserialized.numeroCnj());
        assertEquals(dto.nomeCliente(), deserialized.nomeCliente());
    }

    @Test
    void shouldMapFaturamentoWithProcessoResumo() throws Exception {
        UUID processoId = UUID.randomUUID();
        Processo processo = new Processo();
        processo.setId(processoId);
        processo.setNumeroCnj("1111111-22.2026.8.21.0001");

        Cliente cliente = new Cliente();
        cliente.setNome("Maria Santos");
        processo.setCliente(cliente);

        com.sistemajuridico.backend.core.domain.Faturamento faturamento = new com.sistemajuridico.backend.core.domain.Faturamento();
        faturamento.setId(UUID.randomUUID());
        faturamento.setDescricao("Honorários Iniciais");
        faturamento.setValor(new java.math.BigDecimal("2500.00"));
        faturamento.setTipo(com.sistemajuridico.backend.core.domain.enums.TipoFaturamentoEnum.HONORARIOS);
        faturamento.setStatus(com.sistemajuridico.backend.core.domain.enums.StatusFaturamentoEnum.PENDENTE);
        faturamento.setNatureza(com.sistemajuridico.backend.core.domain.enums.NaturezaFaturamentoEnum.A_RECEBER);
        faturamento.setDataVencimento(java.time.LocalDate.now());
        faturamento.setProcesso(processo);

        FaturamentoDTO dto = FaturamentoDTO.fromEntity(faturamento);

        assertNotNull(dto);
        assertNotNull(dto.processo());
        assertEquals(processoId, dto.processo().id());
        assertEquals("1111111-22.2026.8.21.0001", dto.processo().numeroCnj());
        assertEquals("Maria Santos", dto.processo().nomeCliente());
        assertEquals(processoId, dto.processoId());

        String json = objectMapper.writeValueAsString(dto);
        assertTrue(json.contains("\"processo\":{"));
        assertTrue(json.contains("\"numeroCnj\":\"1111111-22.2026.8.21.0001\""));
        assertTrue(json.contains("\"nomeCliente\":\"Maria Santos\""));
    }

    @Test
    void shouldMapAudienciaWithProcessoResumo() throws Exception {
        UUID processoId = UUID.randomUUID();
        Processo processo = new Processo();
        processo.setId(processoId);
        processo.setNumeroCnj("2222222-33.2026.8.21.0002");

        Cliente cliente = new Cliente();
        cliente.setNome("João Oliveira");
        processo.setCliente(cliente);

        com.sistemajuridico.backend.core.domain.Audiencia audiencia = new com.sistemajuridico.backend.core.domain.Audiencia();
        audiencia.setId(UUID.randomUUID());
        audiencia.setDataHora(java.time.LocalDateTime.now().plusDays(2));
        audiencia.setLocal("1ª Vara Cível");
        audiencia.setStatus(com.sistemajuridico.backend.core.domain.enums.StatusAudienciaEnum.AGENDADA);
        audiencia.setProcesso(processo);

        AudienciaDTO dto = AudienciaDTO.fromEntity(audiencia);

        assertNotNull(dto);
        assertNotNull(dto.processo());
        assertEquals(processoId, dto.processo().id());
        assertEquals("2222222-33.2026.8.21.0002", dto.processo().numeroCnj());
        assertEquals("João Oliveira", dto.processo().nomeCliente());
        assertEquals(processoId, dto.processoId());

        String json = objectMapper.writeValueAsString(dto);
        assertTrue(json.contains("\"processo\":{"));
        assertTrue(json.contains("\"numeroCnj\":\"2222222-33.2026.8.21.0002\""));
        assertTrue(json.contains("\"nomeCliente\":\"João Oliveira\""));
    }

    @Test
    void shouldMapTarefaWithProcessoResumo() throws Exception {
        UUID processoId = UUID.randomUUID();
        Processo processo = new Processo();
        processo.setId(processoId);
        processo.setNumeroCnj("3333333-44.2026.8.21.0003");

        Cliente cliente = new Cliente();
        cliente.setNome("Ana Pereira");
        processo.setCliente(cliente);

        com.sistemajuridico.backend.core.domain.Tarefa tarefa = new com.sistemajuridico.backend.core.domain.Tarefa();
        tarefa.setId(UUID.randomUUID());
        tarefa.setDescricao("Protocolar contestação");
        tarefa.setDataVencimento(java.time.LocalDate.now().plusDays(1));
        tarefa.setConcluida(false);
        tarefa.setProcesso(processo);

        TarefaDTO dto = TarefaDTO.fromEntity(tarefa);

        assertNotNull(dto);
        assertNotNull(dto.processo());
        assertEquals(processoId, dto.processo().id());
        assertEquals("3333333-44.2026.8.21.0003", dto.processo().numeroCnj());
        assertEquals("Ana Pereira", dto.processo().nomeCliente());
        assertEquals(processoId, dto.processoId());

        String json = objectMapper.writeValueAsString(dto);
        assertTrue(json.contains("\"processo\":{"));
        assertTrue(json.contains("\"numeroCnj\":\"3333333-44.2026.8.21.0003\""));
        assertTrue(json.contains("\"nomeCliente\":\"Ana Pereira\""));
    }

    @Test
    void shouldMapProcessoDTOToEntityAndFromEntityWithNewFields() {
        UUID processoId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        UUID advogadoId = UUID.randomUUID();
        java.math.BigDecimal valorCausa = new java.math.BigDecimal("75000.50");

        ProcessoDTO dto = new ProcessoDTO(
                processoId,
                "5001234-88.2026.8.21.0016",
                "Ação Revisional de Contrato",
                com.sistemajuridico.backend.core.domain.enums.FaseProcessualEnum.PROTOCOLADO,
                "Banco do Brasil S.A.",
                "00.000.000/0001-91",
                com.sistemajuridico.backend.core.domain.enums.PapelClienteEnum.AUTOR,
                valorCausa,
                "Ijuí/RS",
                java.time.LocalDate.of(2026, 9, 10),
                clienteId,
                advogadoId
        );

        Processo entity = dto.toEntity();
        assertEquals(processoId, entity.getId());
        assertEquals("5001234-88.2026.8.21.0016", entity.getNumeroCnj());
        assertEquals("Ação Revisional de Contrato", entity.getAssunto());
        assertEquals(com.sistemajuridico.backend.core.domain.enums.FaseProcessualEnum.PROTOCOLADO, entity.getFaseAtual());
        assertEquals("Banco do Brasil S.A.", entity.getParteAdversa());
        assertEquals("00.000.000/0001-91", entity.getCpfCnpjParteAdversa());
        assertEquals(com.sistemajuridico.backend.core.domain.enums.PapelClienteEnum.AUTOR, entity.getPapelCliente());
        assertEquals(valorCausa, entity.getValorCausa());
        assertEquals("Ijuí/RS", entity.getComarca());
        assertEquals(java.time.LocalDate.of(2026, 9, 10), entity.getDataCriacao());

        Cliente cliente = new Cliente();
        cliente.setId(clienteId);
        entity.setCliente(cliente);

        com.sistemajuridico.backend.core.domain.Usuario advogado = new com.sistemajuridico.backend.core.domain.Usuario();
        advogado.setId(advogadoId);
        entity.setAdvogado(advogado);

        ProcessoDTO mappedDto = ProcessoDTO.fromEntity(entity);
        assertNotNull(mappedDto);
        assertEquals(processoId, mappedDto.id());
        assertEquals("5001234-88.2026.8.21.0016", mappedDto.numeroCnj());
        assertEquals("Banco do Brasil S.A.", mappedDto.parteAdversa());
        assertEquals("00.000.000/0001-91", mappedDto.cpfCnpjParteAdversa());
        assertEquals(com.sistemajuridico.backend.core.domain.enums.PapelClienteEnum.AUTOR, mappedDto.papelCliente());
        assertEquals(valorCausa, mappedDto.valorCausa());
        assertEquals("Ijuí/RS", mappedDto.comarca());
        assertEquals(clienteId, mappedDto.clienteId());
        assertEquals(advogadoId, mappedDto.advogadoId());
    }

    @Test
    void shouldMapFaturamentoDTOToEntityAndFromEntityWithNewFields() {
        UUID faturamentoId = UUID.randomUUID();
        UUID processoId = UUID.randomUUID();
        java.math.BigDecimal valor = new java.math.BigDecimal("6000.00");
        java.math.BigDecimal honorarios = new java.math.BigDecimal("2000.00");
        java.math.BigDecimal repasse = new java.math.BigDecimal("4000.00");
        java.time.LocalDate repasseDate = java.time.LocalDate.of(2026, 9, 15);

        FaturamentoDTO dto = new FaturamentoDTO(
                faturamentoId,
                "Honorários e Sucumbência",
                valor,
                com.sistemajuridico.backend.core.domain.enums.TipoFaturamentoEnum.HONORARIOS,
                com.sistemajuridico.backend.core.domain.enums.StatusFaturamentoEnum.PENDENTE,
                com.sistemajuridico.backend.core.domain.enums.NaturezaFaturamentoEnum.A_RECEBER,
                java.time.LocalDate.of(2026, 9, 10),
                null,
                processoId,
                1,
                3,
                com.sistemajuridico.backend.core.domain.enums.OrigemPagamentoEnum.TERCEIRO_SUCUMBENCIA,
                honorarios,
                repasse,
                com.sistemajuridico.backend.core.domain.enums.StatusRepasseEnum.PENDENTE,
                "PIX",
                "Chave PIX: 12345678900",
                repasseDate
        );

        com.sistemajuridico.backend.core.domain.Faturamento entity = dto.toEntity();
        assertEquals(faturamentoId, entity.getId());
        assertEquals("Honorários e Sucumbência", entity.getDescricao());
        assertEquals(valor, entity.getValor());
        assertEquals(Integer.valueOf(1), entity.getNumeroParcela());
        assertEquals(Integer.valueOf(3), entity.getTotalParcelas());
        assertEquals(com.sistemajuridico.backend.core.domain.enums.OrigemPagamentoEnum.TERCEIRO_SUCUMBENCIA, entity.getOrigemPagamento());
        assertEquals(honorarios, entity.getValorHonorariosRetidos());
        assertEquals(repasse, entity.getValorRepasseCliente());
        assertEquals(com.sistemajuridico.backend.core.domain.enums.StatusRepasseEnum.PENDENTE, entity.getStatusRepasse());
        assertEquals("PIX", entity.getFormaRepasse());
        assertEquals("Chave PIX: 12345678900", entity.getDadosBancariosCliente());
        assertEquals(repasseDate, entity.getDataRepasse());

        Processo processo = new Processo();
        processo.setId(processoId);
        processo.setNumeroCnj("5001234-88.2026.8.21.0016");
        entity.setProcesso(processo);

        FaturamentoDTO mappedDto = FaturamentoDTO.fromEntity(entity);
        assertNotNull(mappedDto);
        assertEquals(faturamentoId, mappedDto.id());
        assertEquals(Integer.valueOf(1), mappedDto.numeroParcela());
        assertEquals(Integer.valueOf(3), mappedDto.totalParcelas());
        assertEquals(com.sistemajuridico.backend.core.domain.enums.OrigemPagamentoEnum.TERCEIRO_SUCUMBENCIA, mappedDto.origemPagamento());
        assertEquals(honorarios, mappedDto.valorHonorariosRetidos());
        assertEquals(repasse, mappedDto.valorRepasseCliente());
        assertEquals(com.sistemajuridico.backend.core.domain.enums.StatusRepasseEnum.PENDENTE, mappedDto.statusRepasse());
        assertEquals("PIX", mappedDto.formaRepasse());
        assertEquals("Chave PIX: 12345678900", mappedDto.dadosBancariosCliente());
        assertEquals(repasseDate, mappedDto.dataRepasse());
        assertEquals(processoId, mappedDto.processoId());
    }
}
