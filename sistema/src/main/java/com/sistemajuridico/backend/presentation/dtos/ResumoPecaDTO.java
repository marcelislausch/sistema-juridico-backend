package com.sistemajuridico.backend.presentation.dtos;

import jakarta.validation.constraints.NotBlank;

public record ResumoPecaDTO(
        @NotBlank(message = "O conteúdo da peça processual é obrigatório")
        String conteudoPeca
) {
}
