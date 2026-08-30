package com.sistemajuridico.backend.presentation.dtos;

import com.sistemajuridico.backend.core.domain.Andamento;
import com.sistemajuridico.backend.core.domain.enums.TipoAndamentoEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record AndamentoDTO(
        UUID id,

        LocalDateTime dataHora,

        @NotBlank(message = "A descrição do andamento é obrigatória")
        String descricao,

        @NotNull(message = "O tipo de andamento é obrigatório (AUTOMATICO, MANUAL, IA)")
        TipoAndamentoEnum tipo,

        UUID processoId
) {

    public Andamento toEntity() {
        Andamento andamento = new Andamento();
        andamento.setId(this.id());
        if (this.dataHora() != null) {
            andamento.setDataHora(this.dataHora());
        } else {
            andamento.setDataHora(LocalDateTime.now());
        }
        andamento.setDescricao(this.descricao());
        andamento.setTipo(this.tipo());
        return andamento;
    }

    public static AndamentoDTO fromEntity(Andamento andamento) {
        UUID processoId = null;
        if (andamento.getProcesso() != null) {
            processoId = andamento.getProcesso().getId();
        }
        return new AndamentoDTO(
                andamento.getId(),
                andamento.getDataHora(),
                andamento.getDescricao(),
                andamento.getTipo(),
                processoId
        );
    }
}
