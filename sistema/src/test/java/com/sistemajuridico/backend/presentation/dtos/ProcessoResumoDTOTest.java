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
}
