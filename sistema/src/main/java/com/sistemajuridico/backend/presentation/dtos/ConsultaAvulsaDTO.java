package com.sistemajuridico.backend.presentation.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ConsultaAvulsaDTO(
        @NotNull(message = "O ID do cliente é obrigatório")
        UUID clienteId,

        @NotNull(message = "O valor é obrigatório")
        @Positive(message = "O valor deve ser positivo")
        BigDecimal valor,

        @NotBlank(message = "A descrição da consulta é obrigatória")
        String descricao,

        LocalDate dataPagamento,

        String formaPagamento
) {
    public ConsultaAvulsaDTO(UUID clienteId, BigDecimal valor, String descricao) {
        this(clienteId, valor, descricao, null, null);
    }
}
