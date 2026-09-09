package com.sistemajuridico.backend.presentation.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Estrutura de resposta para erros de validação de campos")
public record ErroValidacaoDTO(
        @Schema(description = "Data e hora em que o erro ocorreu", example = "2026-09-08T22:30:00")
        LocalDateTime timestamp,

        @Schema(description = "Código HTTP do erro", example = "400")
        Integer status,

        @Schema(description = "Identificador ou classificação do erro", example = "Validation Error")
        String error,

        @Schema(description = "Mensagem geral do erro de validação", example = "Erro de validação nos campos informados.")
        String message,

        @Schema(description = "Lista detalhada dos erros encontrados por campo")
        List<CampoErroDTO> fieldErrors
) {
}
