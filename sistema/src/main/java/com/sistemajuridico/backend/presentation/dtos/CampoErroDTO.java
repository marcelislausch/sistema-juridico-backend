package com.sistemajuridico.backend.presentation.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Detalhe do erro de validação em um campo específico")
public record CampoErroDTO(
        @Schema(description = "Nome do campo que falhou na validação", example = "email")
        String campo,

        @Schema(description = "Mensagem descrevendo o motivo da falha", example = "E-mail inválido")
        String mensagem
) {
}
