package com.sistemajuridico.backend.presentation.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.sistemajuridico.backend.core.domain.Processo;

import java.util.UUID;

public record ProcessoResumoDTO(
        UUID id,
        String numeroCnj,
        @JsonAlias({"clienteNome", "nome"})
        String nomeCliente
) {

    public static ProcessoResumoDTO fromEntity(Processo processo) {
        if (processo == null) {
            return null;
        }

        String nomeCliente = null;
        if (processo.getCliente() != null) {
            nomeCliente = processo.getCliente().getNome();
        }

        return new ProcessoResumoDTO(
                processo.getId(),
                processo.getNumeroCnj(),
                nomeCliente
        );
    }
}
