package com.sistemajuridico.backend.presentation.dtos;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sistemajuridico.backend.core.domain.Cliente;
import com.sistemajuridico.backend.core.domain.Documento;
import com.sistemajuridico.backend.core.domain.Processo;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProcessoResumoDTOTest {

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void shouldMapFromEntityCorrectly() {
        UUID processoId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        Processo processo = new Processo();
        processo.setId(processoId);
        processo.setNumeroCnj("5001234-56.2026.8.21.0016");

        Cliente cliente = new Cliente();
        cliente.setId(clienteId);
        cliente.setNome("Carlos Silva");
        processo.setCliente(cliente);

        ProcessoResumoDTO dto = ProcessoResumoDTO.fromEntity(processo);

        assertNotNull(dto);
        assertEquals(processoId, dto.id());
        assertEquals("5001234-56.2026.8.21.0016", dto.numeroCnj());
        assertEquals(clienteId, dto.clienteId());
        assertEquals("Carlos Silva", dto.nomeCliente());
    }

    @Test
    void shouldSerializeAndDeserializeJson() throws Exception {
        UUID processoId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        ProcessoResumoDTO dto = new ProcessoResumoDTO(processoId, "5001234-56.2026.8.21.0016", clienteId, "Carlos Silva");

        String json = objectMapper.writeValueAsString(dto);
        assertTrue(json.contains("\"nomeCliente\":\"Carlos Silva\""));
        assertTrue(json.contains("\"clienteId\":\"" + clienteId + "\""));

        ProcessoResumoDTO deserialized = objectMapper.readValue(json, ProcessoResumoDTO.class);
        assertEquals(dto.id(), deserialized.id());
        assertEquals(dto.numeroCnj(), deserialized.numeroCnj());
        assertEquals(dto.clienteId(), deserialized.clienteId());
        assertEquals(dto.nomeCliente(), deserialized.nomeCliente());
    }

    @Test
    void shouldMapFaturamentoWithProcessoResumo() throws Exception {
        UUID processoId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        Processo processo = new Processo();
        processo.setId(processoId);
        processo.setNumeroCnj("1111111-22.2026.8.21.0001");

        Cliente cliente = new Cliente();
        cliente.setId(clienteId);
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
        assertEquals(clienteId, dto.processo().clienteId());
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
        UUID clienteId = UUID.randomUUID();
        UUID advogadoId = UUID.randomUUID();

        Cliente cliente = new Cliente();
        cliente.setId(clienteId);
        cliente.setNome("João Oliveira");

        com.sistemajuridico.backend.core.domain.Usuario advogado = new com.sistemajuridico.backend.core.domain.Usuario();
        advogado.setId(advogadoId);
        advogado.setNome("Dra. Marceli");
        advogado.setEmail("marceli@advocacia.com");
        advogado.setOab("RS123456");
        advogado.setPerfil(com.sistemajuridico.backend.core.domain.enums.PerfilAcessoEnum.ADVOGADO);

        Processo processo = new Processo();
        processo.setId(processoId);
        processo.setNumeroCnj("2222222-33.2026.8.21.0002");
        processo.setCliente(cliente);
        processo.setAdvogado(advogado);

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
        assertEquals(clienteId, dto.processo().clienteId());
        assertEquals("João Oliveira", dto.processo().nomeCliente());
        assertEquals(processoId, dto.processoId());
        assertEquals(advogadoId, dto.responsavelId());
        assertNotNull(dto.responsavel());
        assertEquals("Dra. Marceli", dto.responsavel().nome());
        assertEquals("RS123456", dto.responsavel().oab());

        String json = objectMapper.writeValueAsString(dto);
        assertTrue(json.contains("\"processo\":{"));
        assertTrue(json.contains("\"numeroCnj\":\"2222222-33.2026.8.21.0002\""));
        assertTrue(json.contains("\"nomeCliente\":\"João Oliveira\""));
        assertTrue(json.contains("\"responsavel\":{"));
        assertTrue(json.contains("\"nome\":\"Dra. Marceli\""));
    }

    @Test
    void shouldMapTarefaWithProcessoResumo() throws Exception {
        UUID processoId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();

        Cliente cliente = new Cliente();
        cliente.setId(clienteId);
        cliente.setNome("Ana Pereira");

        Processo processo = new Processo();
        processo.setId(processoId);
        processo.setNumeroCnj("3333333-44.2026.8.21.0003");
        processo.setCliente(cliente);

        com.sistemajuridico.backend.core.domain.Usuario usuario = new com.sistemajuridico.backend.core.domain.Usuario();
        usuario.setId(usuarioId);
        usuario.setNome("Dr. Paulo");
        usuario.setEmail("paulo@advocacia.com");
        usuario.setOab("RS654321");
        usuario.setPerfil(com.sistemajuridico.backend.core.domain.enums.PerfilAcessoEnum.ADVOGADO);

        com.sistemajuridico.backend.core.domain.Tarefa tarefa = new com.sistemajuridico.backend.core.domain.Tarefa();
        tarefa.setId(UUID.randomUUID());
        tarefa.setDescricao("Protocolar contestação");
        tarefa.setDataVencimento(java.time.LocalDate.now().plusDays(1));
        tarefa.setConcluida(false);
        tarefa.setProcesso(processo);
        tarefa.setUsuario(usuario);

        TarefaDTO dto = TarefaDTO.fromEntity(tarefa);

        assertNotNull(dto);
        assertNotNull(dto.processo());
        assertEquals(processoId, dto.processo().id());
        assertEquals("3333333-44.2026.8.21.0003", dto.processo().numeroCnj());
        assertEquals(clienteId, dto.processo().clienteId());
        assertEquals("Ana Pereira", dto.processo().nomeCliente());
        assertEquals(processoId, dto.processoId());
        assertEquals(usuarioId, dto.usuarioId());
        assertNotNull(dto.usuario());
        assertEquals("Dr. Paulo", dto.usuario().nome());
        assertEquals("RS654321", dto.usuario().oab());

        String json = objectMapper.writeValueAsString(dto);
        assertTrue(json.contains("\"processo\":{"));
        assertTrue(json.contains("\"numeroCnj\":\"3333333-44.2026.8.21.0003\""));
        assertTrue(json.contains("\"nomeCliente\":\"Ana Pereira\""));
        assertTrue(json.contains("\"usuario\":{"));
        assertTrue(json.contains("\"nome\":\"Dr. Paulo\""));
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
        cliente.setNome("Empresa ABC");
        cliente.setCpfCnpj("12.345.678/0001-99");
        cliente.setTipo(com.sistemajuridico.backend.core.domain.enums.TipoClienteEnum.JURIDICA);
        entity.setCliente(cliente);

        com.sistemajuridico.backend.core.domain.Usuario advogado = new com.sistemajuridico.backend.core.domain.Usuario();
        advogado.setId(advogadoId);
        advogado.setNome("Dra. Marceli");
        advogado.setEmail("marceli@advocacia.com");
        advogado.setOab("RS123456");
        advogado.setPerfil(com.sistemajuridico.backend.core.domain.enums.PerfilAcessoEnum.ADVOGADO);
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
        assertNotNull(mappedDto.cliente());
        assertEquals("Empresa ABC", mappedDto.cliente().nome());
        assertEquals("12.345.678/0001-99", mappedDto.cliente().cpfCnpj());
        assertEquals(com.sistemajuridico.backend.core.domain.enums.TipoClienteEnum.JURIDICA, mappedDto.cliente().tipo());
        assertNotNull(mappedDto.advogado());
        assertEquals("Dra. Marceli", mappedDto.advogado().nome());
        assertEquals("marceli@advocacia.com", mappedDto.advogado().email());
        assertEquals("RS123456", mappedDto.advogado().oab());
        assertEquals(com.sistemajuridico.backend.core.domain.enums.PerfilAcessoEnum.ADVOGADO, mappedDto.advogado().perfil());
    }

    @Test
    void shouldSerializeAndDeserializeProcessoDTOWithNestedObjects() throws Exception {
        UUID processoId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        UUID advogadoId = UUID.randomUUID();

        ClienteResumoDTO cliente = new ClienteResumoDTO(clienteId, "Empresa Alpha", "12.345.678/0001-99", com.sistemajuridico.backend.core.domain.enums.TipoClienteEnum.JURIDICA);
        UsuarioResumoDTO advogado = new UsuarioResumoDTO(advogadoId, "Dr. Roberto", "roberto@adv.com", "OAB/RS 99999", com.sistemajuridico.backend.core.domain.enums.PerfilAcessoEnum.ADVOGADO);

        ProcessoDTO dto = new ProcessoDTO(
                processoId,
                "5009999-11.2026.8.21.0016",
                "Ação Ordinária",
                com.sistemajuridico.backend.core.domain.enums.FaseProcessualEnum.EM_ANDAMENTO,
                "Empresa Beta",
                "98.765.432/0001-10",
                com.sistemajuridico.backend.core.domain.enums.PapelClienteEnum.AUTOR,
                new java.math.BigDecimal("150000.00"),
                "Porto Alegre/RS",
                java.time.LocalDate.of(2026, 9, 10),
                cliente,
                advogado
        );

        String json = objectMapper.writeValueAsString(dto);
        assertTrue(json.contains("\"cliente\":{"));
        assertTrue(json.contains("\"nome\":\"Empresa Alpha\""));
        assertTrue(json.contains("\"advogado\":{"));
        assertTrue(json.contains("\"oab\":\"OAB/RS 99999\""));

        ProcessoDTO deserialized = objectMapper.readValue(json, ProcessoDTO.class);
        assertNotNull(deserialized);
        assertEquals(processoId, deserialized.id());
        assertEquals(clienteId, deserialized.clienteId());
        assertEquals(advogadoId, deserialized.advogadoId());
        assertEquals("Empresa Alpha", deserialized.cliente().nome());
        assertEquals("Dr. Roberto", deserialized.advogado().nome());
    }

    @Test
    void shouldDeserializeProcessoDTOWithLegacyFlatIds() throws Exception {
        UUID clienteId = UUID.randomUUID();
        UUID advogadoId = UUID.randomUUID();

        String json = """
                {
                    "numeroCnj": "5008888-22.2026.8.21.0016",
                    "assunto": "Ação de Cobrança",
                    "clienteId": "%s",
                    "advogadoId": "%s"
                }
                """.formatted(clienteId, advogadoId);

        ProcessoDTO deserialized = objectMapper.readValue(json, ProcessoDTO.class);
        assertNotNull(deserialized);
        assertEquals("5008888-22.2026.8.21.0016", deserialized.numeroCnj());
        assertEquals("Ação de Cobrança", deserialized.assunto());
        assertEquals(clienteId, deserialized.clienteId());
        assertEquals(advogadoId, deserialized.advogadoId());
        assertNotNull(deserialized.cliente());
        assertEquals(clienteId, deserialized.cliente().id());
        assertNotNull(deserialized.advogado());
        assertEquals(advogadoId, deserialized.advogado().id());
    }

    @Test
    void shouldDeserializeProcessoDTOWithDirectStringIds() throws Exception {
        UUID clienteId = UUID.randomUUID();
        UUID advogadoId = UUID.randomUUID();

        String json = """
                {
                    "numeroCnj": "5007777-33.2026.8.21.0016",
                    "cliente": "%s",
                    "advogado": "%s"
                }
                """.formatted(clienteId, advogadoId);

        ProcessoDTO deserialized = objectMapper.readValue(json, ProcessoDTO.class);
        assertNotNull(deserialized);
        assertEquals("5007777-33.2026.8.21.0016", deserialized.numeroCnj());
        assertEquals(clienteId, deserialized.clienteId());
        assertEquals(advogadoId, deserialized.advogadoId());
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

    @Test
    void shouldSerializeAndDeserializeAudienciaDTOWithNestedAndLegacyIds() throws Exception {
        UUID audienciaId = UUID.randomUUID();
        UUID processoId = UUID.randomUUID();
        UUID responsavelId = UUID.randomUUID();

        UsuarioResumoDTO responsavel = new UsuarioResumoDTO(responsavelId, "Dra. Marceli", "marceli@adv.com", "OAB/RS 123", com.sistemajuridico.backend.core.domain.enums.PerfilAcessoEnum.ADVOGADO);
        ProcessoResumoDTO processo = new ProcessoResumoDTO(processoId, "5001111-22.2026.8.21.0001", UUID.randomUUID(), "Cliente Teste");

        AudienciaDTO dto = new AudienciaDTO(
                audienciaId,
                java.time.LocalDateTime.of(2026, 9, 20, 14, 30),
                "Sala de Audiências 1",
                "Audiência de Instrução",
                com.sistemajuridico.backend.core.domain.enums.StatusAudienciaEnum.AGENDADA,
                null,
                processo,
                responsavel
        );

        String json = objectMapper.writeValueAsString(dto);
        assertTrue(json.contains("\"responsavel\":{"));
        assertTrue(json.contains("\"nome\":\"Dra. Marceli\""));
        assertTrue(json.contains("\"processo\":{"));

        // Desserialização com objeto aninhado
        AudienciaDTO deserialized = objectMapper.readValue(json, AudienciaDTO.class);
        assertNotNull(deserialized);
        assertEquals(audienciaId, deserialized.id());
        assertEquals(processoId, deserialized.processoId());
        assertEquals(responsavelId, deserialized.responsavelId());
        assertEquals("Dra. Marceli", deserialized.responsavel().nome());

        // Desserialização com ID legado
        String legacyJson = """
                {
                    "dataHora": "2026-09-20T14:30:00",
                    "local": "Sala 1",
                    "processoId": "%s",
                    "responsavelId": "%s"
                }
                """.formatted(processoId, responsavelId);

        AudienciaDTO legacyDeserialized = objectMapper.readValue(legacyJson, AudienciaDTO.class);
        assertNotNull(legacyDeserialized);
        assertEquals(processoId, legacyDeserialized.processoId());
        assertEquals(responsavelId, legacyDeserialized.responsavelId());
        assertNotNull(legacyDeserialized.responsavel());
        assertEquals(responsavelId, legacyDeserialized.responsavel().id());

        // Desserialização com direct string ID
        String directJson = """
                {
                    "dataHora": "2026-09-20T14:30:00",
                    "local": "Sala 1",
                    "processo": "%s",
                    "responsavel": "%s"
                }
                """.formatted(processoId, responsavelId);

        AudienciaDTO directDeserialized = objectMapper.readValue(directJson, AudienciaDTO.class);
        assertNotNull(directDeserialized);
        assertEquals(processoId, directDeserialized.processoId());
        assertEquals(responsavelId, directDeserialized.responsavelId());
    }

    @Test
    void shouldSerializeAndDeserializeTarefaDTOWithNestedAndLegacyIds() throws Exception {
        UUID tarefaId = UUID.randomUUID();
        UUID processoId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();

        UsuarioResumoDTO usuario = new UsuarioResumoDTO(usuarioId, "Dr. Paulo", "paulo@adv.com", "OAB/RS 456", com.sistemajuridico.backend.core.domain.enums.PerfilAcessoEnum.ADVOGADO);
        ProcessoResumoDTO processo = new ProcessoResumoDTO(processoId, "5002222-33.2026.8.21.0001", UUID.randomUUID(), "Cliente Teste");

        TarefaDTO dto = new TarefaDTO(
                tarefaId,
                "Elaborar Réplica",
                java.time.LocalDate.of(2026, 9, 25),
                false,
                com.sistemajuridico.backend.core.domain.enums.TipoTarefaEnum.PRAZO,
                usuario,
                processo
        );

        String json = objectMapper.writeValueAsString(dto);
        assertTrue(json.contains("\"usuario\":{"));
        assertTrue(json.contains("\"nome\":\"Dr. Paulo\""));
        assertTrue(json.contains("\"processo\":{"));

        // Desserialização com objeto aninhado
        TarefaDTO deserialized = objectMapper.readValue(json, TarefaDTO.class);
        assertNotNull(deserialized);
        assertEquals(tarefaId, deserialized.id());
        assertEquals(processoId, deserialized.processoId());
        assertEquals(usuarioId, deserialized.usuarioId());
        assertEquals("Dr. Paulo", deserialized.usuario().nome());

        // Desserialização com ID legado
        String legacyJson = """
                {
                    "descricao": "Elaborar Réplica",
                    "dataVencimento": "2026-09-25",
                    "processoId": "%s",
                    "usuarioId": "%s"
                }
                """.formatted(processoId, usuarioId);

        TarefaDTO legacyDeserialized = objectMapper.readValue(legacyJson, TarefaDTO.class);
        assertNotNull(legacyDeserialized);
        assertEquals(processoId, legacyDeserialized.processoId());
        assertEquals(usuarioId, legacyDeserialized.usuarioId());
        assertNotNull(legacyDeserialized.usuario());
        assertEquals(usuarioId, legacyDeserialized.usuario().id());

        // Desserialização com direct string ID
        String directJson = """
                {
                    "descricao": "Elaborar Réplica",
                    "dataVencimento": "2026-09-25",
                    "processo": "%s",
                    "usuario": "%s"
                }
                """.formatted(processoId, usuarioId);

        TarefaDTO directDeserialized = objectMapper.readValue(directJson, TarefaDTO.class);
        assertNotNull(directDeserialized);
        assertEquals(processoId, directDeserialized.processoId());
        assertEquals(usuarioId, directDeserialized.usuarioId());
    }

    @Test
    void shouldMapDocumentoDTOFromEntityWithNestedObjects() {
        UUID documentoId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        UUID processoId = UUID.randomUUID();

        Cliente cliente = new Cliente();
        cliente.setId(clienteId);
        cliente.setNome("Maria Ferreira");
        cliente.setCpfCnpj("111.222.333-44");
        cliente.setTipo(com.sistemajuridico.backend.core.domain.enums.TipoClienteEnum.FISICA);

        Processo processo = new Processo();
        processo.setId(processoId);
        processo.setNumeroCnj("5003333-44.2026.8.21.0001");
        processo.setCliente(cliente);

        Documento documento = new Documento();
        documento.setId(documentoId);
        documento.setNomeArquivo("peticao_inicial.pdf");
        documento.setTitulo("Petição Inicial Digitalizada");
        documento.setCaminhoStorage("uploads/ged/2026/09/peticao_inicial.pdf");
        documento.setIndexadoIA(true);
        documento.setCliente(cliente);
        documento.setProcesso(processo);

        DocumentoDTO dto = DocumentoDTO.fromEntity(documento);

        assertNotNull(dto);
        assertEquals(documentoId, dto.id());
        assertEquals("peticao_inicial.pdf", dto.nomeArquivo());
        assertEquals("Petição Inicial Digitalizada", dto.titulo());
        assertEquals("uploads/ged/2026/09/peticao_inicial.pdf", dto.caminhoStorage());
        assertTrue(dto.indexadoIA());

        assertEquals(clienteId, dto.clienteId());
        assertNotNull(dto.cliente());
        assertEquals(clienteId, dto.cliente().id());
        assertEquals("Maria Ferreira", dto.cliente().nome());
        assertEquals("111.222.333-44", dto.cliente().cpfCnpj());
        assertEquals(com.sistemajuridico.backend.core.domain.enums.TipoClienteEnum.FISICA, dto.cliente().tipo());

        assertEquals(processoId, dto.processoId());
        assertNotNull(dto.processo());
        assertEquals(processoId, dto.processo().id());
        assertEquals("5003333-44.2026.8.21.0001", dto.processo().numeroCnj());
        assertEquals(clienteId, dto.processo().clienteId());
        assertEquals("Maria Ferreira", dto.processo().nomeCliente());
    }

    @Test
    void shouldSerializeAndDeserializeDocumentoDTOWithNestedAndLegacyIds() throws Exception {
        UUID documentoId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        UUID processoId = UUID.randomUUID();

        ClienteResumoDTO cliente = new ClienteResumoDTO(clienteId, "Pedro Alvares", "222.333.444-55", com.sistemajuridico.backend.core.domain.enums.TipoClienteEnum.FISICA);
        ProcessoResumoDTO processo = new ProcessoResumoDTO(processoId, "5004444-55.2026.8.21.0001", clienteId, "Pedro Alvares");

        DocumentoDTO dto = new DocumentoDTO(
                documentoId,
                "contrato_social.pdf",
                "Contrato Social da Empresa",
                "uploads/ged/contrato_social.pdf",
                false,
                cliente,
                processo
        );

        String json = objectMapper.writeValueAsString(dto);
        assertTrue(json.contains("\"cliente\":{"));
        assertTrue(json.contains("\"nome\":\"Pedro Alvares\""));
        assertTrue(json.contains("\"processo\":{"));
        assertTrue(json.contains("\"numeroCnj\":\"5004444-55.2026.8.21.0001\""));

        // Desserialização com objetos aninhados
        DocumentoDTO deserialized = objectMapper.readValue(json, DocumentoDTO.class);
        assertNotNull(deserialized);
        assertEquals(documentoId, deserialized.id());
        assertEquals(clienteId, deserialized.clienteId());
        assertEquals(processoId, deserialized.processoId());
        assertEquals("Pedro Alvares", deserialized.cliente().nome());
        assertEquals("5004444-55.2026.8.21.0001", deserialized.processo().numeroCnj());

        // Desserialização com IDs legados
        String legacyJson = """
                {
                    "nomeArquivo": "contrato_social.pdf",
                    "titulo": "Contrato Social da Empresa",
                    "caminhoStorage": "uploads/ged/contrato_social.pdf",
                    "indexadoIA": false,
                    "clienteId": "%s",
                    "processoId": "%s"
                }
                """.formatted(clienteId, processoId);

        DocumentoDTO legacyDeserialized = objectMapper.readValue(legacyJson, DocumentoDTO.class);
        assertNotNull(legacyDeserialized);
        assertEquals(clienteId, legacyDeserialized.clienteId());
        assertEquals(processoId, legacyDeserialized.processoId());
        assertNotNull(legacyDeserialized.cliente());
        assertEquals(clienteId, legacyDeserialized.cliente().id());
        assertNotNull(legacyDeserialized.processo());
        assertEquals(processoId, legacyDeserialized.processo().id());

        // Desserialização com direct string IDs
        String directJson = """
                {
                    "nomeArquivo": "contrato_social.pdf",
                    "cliente": "%s",
                    "processo": "%s"
                }
                """.formatted(clienteId, processoId);

        DocumentoDTO directDeserialized = objectMapper.readValue(directJson, DocumentoDTO.class);
        assertNotNull(directDeserialized);
        assertEquals(clienteId, directDeserialized.clienteId());
        assertEquals(processoId, directDeserialized.processoId());
    }
}
