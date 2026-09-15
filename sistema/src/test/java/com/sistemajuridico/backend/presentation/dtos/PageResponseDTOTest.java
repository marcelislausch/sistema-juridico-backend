package com.sistemajuridico.backend.presentation.dtos;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PageResponseDTOTest {

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void shouldCreatePageMetadataDTOProperly() {
        PageMetadataDTO metadata = new PageMetadataDTO(20, 0, 125L, 7);

        assertEquals(20, metadata.size());
        assertEquals(0, metadata.number());
        assertEquals(125L, metadata.totalElements());
        assertEquals(7, metadata.totalPages());
    }

    @Test
    void shouldSerializeClientePageResponseAccordingToContract() throws Exception {
        Page<ClienteDTO> page = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 20), 0);
        ClientePageResponse response = ClientePageResponse.from(page);

        assertNotNull(response);
        assertNotNull(response.content());
        assertTrue(response.content().isEmpty());
        assertEquals(20, response.page().size());
        assertEquals(0, response.page().number());
        assertEquals(0L, response.page().totalElements());
        assertEquals(0, response.page().totalPages());

        String json = objectMapper.writeValueAsString(response);
        JsonNode node = objectMapper.readTree(json);

        assertTrue(node.has("content"));
        assertTrue(node.get("content").isArray());
        assertEquals(0, node.get("content").size());

        assertTrue(node.has("page"));
        JsonNode pageNode = node.get("page");
        assertEquals(20, pageNode.get("size").asInt());
        assertEquals(0, pageNode.get("number").asInt());
        assertEquals(0, pageNode.get("totalElements").asLong());
        assertEquals(0, pageNode.get("totalPages").asInt());
    }

    @Test
    void shouldSerializeProcessoPageResponseProperly() throws Exception {
        Page<ProcessoDTO> page = new PageImpl<>(Collections.emptyList(), PageRequest.of(1, 10), 45);
        ProcessoPageResponse response = ProcessoPageResponse.from(page);

        assertNotNull(response);
        assertEquals(10, response.page().size());
        assertEquals(1, response.page().number());
        assertEquals(45L, response.page().totalElements());
        assertEquals(5, response.page().totalPages());
    }

    @Test
    void shouldSerializeUsuarioPageResponseProperly() throws Exception {
        Page<UsuarioResponseDTO> page = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 20), 10);
        UsuarioPageResponse response = UsuarioPageResponse.from(page);

        assertNotNull(response);
        assertEquals(20, response.page().size());
        assertEquals(0, response.page().number());
        assertEquals(10L, response.page().totalElements());
        assertEquals(1, response.page().totalPages());
    }

    @Test
    void shouldSerializeFaturamentoPageResponseProperly() throws Exception {
        Page<FaturamentoDTO> page = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
        FaturamentoPageResponse response = FaturamentoPageResponse.from(page);

        assertNotNull(response);
        assertEquals(10, response.page().size());
        assertEquals(0, response.page().number());
        assertEquals(0L, response.page().totalElements());
        assertEquals(0, response.page().totalPages());
    }

    @Test
    void shouldHandleNullPageDefensively() {
        ClientePageResponse clienteResponse = ClientePageResponse.from(null);
        assertNotNull(clienteResponse.content());
        assertTrue(clienteResponse.content().isEmpty());
        assertNotNull(clienteResponse.page());
        assertEquals(0, clienteResponse.page().totalElements());

        ProcessoPageResponse processoResponse = ProcessoPageResponse.from(null);
        assertNotNull(processoResponse.content());
        assertTrue(processoResponse.content().isEmpty());

        UsuarioPageResponse usuarioResponse = UsuarioPageResponse.from(null);
        assertNotNull(usuarioResponse.content());
        assertTrue(usuarioResponse.content().isEmpty());

        FaturamentoPageResponse faturamentoResponse = FaturamentoPageResponse.from(null);
        assertNotNull(faturamentoResponse.content());
        assertTrue(faturamentoResponse.content().isEmpty());
    }
}
