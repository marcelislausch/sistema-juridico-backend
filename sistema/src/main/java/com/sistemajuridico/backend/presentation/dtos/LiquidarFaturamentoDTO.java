package com.sistemajuridico.backend.presentation.dtos;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record LiquidarFaturamentoDTO(
        @NotNull(message = "A data de pagamento é obrigatória para liquidar a fatura")
        LocalDate dataPagamento
) {
}
