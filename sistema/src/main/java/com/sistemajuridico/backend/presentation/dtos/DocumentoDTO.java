package com.sistemajuridico.backend.presentation.dtos;

import com.sistemajuridico.backend.core.domain.Documento;

import java.util.UUID;

public record DocumentoDTO(
        UUID id,
        String nomeArquivo,
        String titulo,
        String caminhoStorage,
        Boolean indexadoIA,
        UUID clienteId,
        UUID processoId
) {

    public static DocumentoDTO fromEntity(Documento documento) {
        UUID clienteId = null;
        if (documento.getCliente() != null) {
            clienteId = documento.getCliente().getId();
        }

        UUID processoId = null;
        if (documento.getProcesso() != null) {
            processoId = documento.getProcesso().getId();
        }

        return new DocumentoDTO(
                documento.getId(),
                documento.getNomeArquivo(),
                documento.getTitulo(),
                documento.getCaminhoStorage(),
                documento.getIndexadoIA(),
                clienteId,
                processoId
        );
    }
}
