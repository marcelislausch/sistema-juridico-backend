package com.sistemajuridico.backend.presentation.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.sistemajuridico.backend.core.domain.Processo;

import java.util.UUID;

public record ProcessoResumoDTO(
        UUID id,
        String numeroCnj,
        UUID clienteId,
        @JsonAlias({"clienteNome", "nome"})
        String nomeCliente
) {

    public ProcessoResumoDTO(UUID id) {
        this(id, null, null, null);
    }

    public ProcessoResumoDTO(UUID id, String numeroCnj, String nomeCliente) {
        this(id, numeroCnj, null, nomeCliente);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ProcessoResumoDTO fromId(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        return new ProcessoResumoDTO(UUID.fromString(id));
    }

    public static ProcessoResumoDTO fromEntity(Processo processo) {
        if (processo == null) {
            return null;
        }

        UUID clienteId = null;
        String nomeCliente = null;
        if (processo.getCliente() != null) {
            clienteId = processo.getCliente().getId();
            nomeCliente = processo.getCliente().getNome();
        }

        return new ProcessoResumoDTO(
                processo.getId(),
                processo.getNumeroCnj(),
                clienteId,
                nomeCliente
        );
    }
}
