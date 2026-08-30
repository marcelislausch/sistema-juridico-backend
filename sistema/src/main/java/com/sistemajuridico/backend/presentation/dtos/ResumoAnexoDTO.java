package com.sistemajuridico.backend.presentation.dtos;

import jakarta.validation.constraints.NotBlank;

public record ResumoAnexoDTO(
        @NotBlank(message = "O resumo gerado pela IA é obrigatório")
        String resumo
) {
}
