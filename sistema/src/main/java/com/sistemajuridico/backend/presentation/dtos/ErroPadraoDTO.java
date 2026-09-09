package com.sistemajuridico.backend.presentation.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Estrutura padrão de resposta para erros da API")
public record ErroPadraoDTO(
        @Schema(description = "Data e hora em que o erro ocorreu", example = "2026-09-08T22:30:00")
        LocalDateTime timestamp,

        @Schema(description = "Código HTTP do erro", example = "400")
        Integer status,

        @Schema(description = "Identificador ou classificação do erro", example = "Bad Request")
        String error,

        @Schema(description = "Descrição detalhada da mensagem de erro", example = "Dados inválidos fornecidos")
        String message
) {
}
