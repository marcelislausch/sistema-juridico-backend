package com.juridia.sistema.presentation.dtos;

import com.juridia.sistema.core.domain.Andamento;
import com.juridia.sistema.core.domain.enums.TipoAndamentoEnum;

import java.time.LocalDateTime;
import java.util.UUID;

public record AndamentoDTO(
        UUID id,
        LocalDateTime dataHora,
        String descricao,
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
