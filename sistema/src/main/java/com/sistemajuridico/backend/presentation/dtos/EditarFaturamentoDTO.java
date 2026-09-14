package com.sistemajuridico.backend.presentation.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.sistemajuridico.backend.core.domain.enums.NaturezaFaturamentoEnum;
import com.sistemajuridico.backend.core.domain.enums.StatusFaturamentoEnum;
import com.sistemajuridico.backend.core.domain.enums.TipoFaturamentoEnum;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record EditarFaturamentoDTO(
        String descricao,

        @Positive(message = "O valor deve ser positivo")
        BigDecimal valor,

        @JsonAlias({"categoria", "tipo"})
        TipoFaturamentoEnum categoria,

        StatusFaturamentoEnum status,

        NaturezaFaturamentoEnum natureza,

        LocalDate dataVencimento,

        LocalDate dataPagamento,

        UUID processoId
) {
    public TipoFaturamentoEnum tipo() {
        return this.categoria;
    }
}
