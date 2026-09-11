package com.sistemajuridico.backend.presentation.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sistemajuridico.backend.core.domain.Documento;

import java.util.UUID;

public record DocumentoDTO(
        UUID id,
        String nomeArquivo,
        String titulo,
        String caminhoStorage,
        Boolean indexadoIA,
        @JsonAlias({"clienteId"})
        ClienteResumoDTO cliente,
        @JsonAlias({"processoId"})
        ProcessoResumoDTO processo
) {

    public DocumentoDTO(
            UUID id,
            String nomeArquivo,
            String titulo,
            String caminhoStorage,
            Boolean indexadoIA,
            UUID clienteId,
            UUID processoId
    ) {
        this(
                id,
                nomeArquivo,
                titulo,
                caminhoStorage,
                indexadoIA,
                clienteId != null ? new ClienteResumoDTO(clienteId) : null,
                processoId != null ? new ProcessoResumoDTO(processoId) : null
        );
    }

    public DocumentoDTO(
            UUID id,
            String nomeArquivo,
            String titulo,
            String caminhoStorage,
            Boolean indexadoIA,
            ClienteResumoDTO cliente,
            UUID processoId
    ) {
        this(
                id,
                nomeArquivo,
                titulo,
                caminhoStorage,
                indexadoIA,
                cliente,
                processoId != null ? new ProcessoResumoDTO(processoId) : null
        );
    }

    public DocumentoDTO(
            UUID id,
            String nomeArquivo,
            String titulo,
            String caminhoStorage,
            Boolean indexadoIA,
            UUID clienteId,
            ProcessoResumoDTO processo
    ) {
        this(
                id,
                nomeArquivo,
                titulo,
                caminhoStorage,
                indexadoIA,
                clienteId != null ? new ClienteResumoDTO(clienteId) : null,
                processo
        );
    }

    @JsonIgnore
    public UUID clienteId() {
        return this.cliente != null ? this.cliente.id() : null;
    }

    @JsonIgnore
    public UUID processoId() {
        return this.processo != null ? this.processo.id() : null;
    }

    public static DocumentoDTO fromEntity(Documento documento) {
        if (documento == null) {
            return null;
        }

        ClienteResumoDTO cliente = null;
        if (documento.getCliente() != null) {
            cliente = ClienteResumoDTO.fromEntity(documento.getCliente());
        }

        ProcessoResumoDTO processo = null;
        if (documento.getProcesso() != null) {
            processo = ProcessoResumoDTO.fromEntity(documento.getProcesso());
        }

        return new DocumentoDTO(
                documento.getId(),
                documento.getNomeArquivo(),
                documento.getTitulo(),
                documento.getCaminhoStorage(),
                documento.getIndexadoIA(),
                cliente,
                processo
        );
    }
}
