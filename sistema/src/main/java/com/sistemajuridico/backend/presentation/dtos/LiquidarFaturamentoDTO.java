package com.sistemajuridico.backend.presentation.dtos;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record LiquidarFaturamentoDTO(
        LocalDate dataPagamento
) {
}
