package com.juridia.sistema.presentation.dtos;

import com.juridia.sistema.core.domain.Processo;
import java.time.LocalDate;
import java.util.UUID;

public record ProcessoDTO(
        UUID id,
        String numeroCnj,
        String assunto,
        String faseAtual,
        LocalDate dataCriacao,
        UUID clienteId,
        UUID advogadoId
) {

    public Processo toEntity() {
        Processo processo = new Processo();
        processo.setId(this.id());
        processo.setNumeroCnj(this.numeroCnj());
        processo.setAssunto(this.assunto());
        processo.setFaseAtual(this.faseAtual());
        processo.setDataCriacao(this.dataCriacao());
        return processo;
    }

    public static ProcessoDTO fromEntity(Processo processo) {
        UUID clienteId = null;
        if (processo.getCliente() != null) {
            clienteId = processo.getCliente().getId();
        }

        UUID advogadoId = null;
        if (processo.getAdvogado() != null) {
            advogadoId = processo.getAdvogado().getId();
        }

        return new ProcessoDTO(
                processo.getId(),
                processo.getNumeroCnj(),
                processo.getAssunto(),
                processo.getFaseAtual(),
                processo.getDataCriacao(),
                clienteId,
                advogadoId
        );
    }
}