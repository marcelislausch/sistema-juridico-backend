package com.sistemajuridico.backend.presentation.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LiquidarParcialDTO(
        @NotNull(message = "O valor pago é obrigatório")
        @Positive(message = "O valor pago deve ser positivo")
        BigDecimal valorPago,

        LocalDate dataPagamento,

        @NotNull(message = "A nova data de vencimento é obrigatória para o saldo remanescente")
        LocalDate novaDataVencimento
) {
}
