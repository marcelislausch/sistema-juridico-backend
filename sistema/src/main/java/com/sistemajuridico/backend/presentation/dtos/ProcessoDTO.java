package com.sistemajuridico.backend.presentation.dtos;

import com.sistemajuridico.backend.core.domain.Processo;
import com.sistemajuridico.backend.core.domain.enums.FaseProcessualEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record ProcessoDTO(
        UUID id,

        @NotBlank(message = "O número CNJ é obrigatório")
        String numeroCnj,

        String assunto,

        FaseProcessualEnum faseAtual,

        LocalDate dataCriacao,

        @NotNull(message = "O ID do cliente é obrigatório")
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
