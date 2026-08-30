package com.sistemajuridico.backend.presentation.dtos;

import jakarta.validation.constraints.NotBlank;

public record GerarResumoAudienciaDTO(
        @NotBlank(message = "O conteúdo da peça processual é obrigatório para gerar o resumo")
        String conteudoPeca
) {
}
