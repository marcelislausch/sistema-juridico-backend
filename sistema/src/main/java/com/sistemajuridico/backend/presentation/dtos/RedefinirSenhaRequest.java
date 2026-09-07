package com.sistemajuridico.backend.presentation.dtos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record RedefinirSenhaRequest(
        @JsonProperty("token")
        @NotBlank(message = "O token de recuperação é obrigatório")
        String token,

        @JsonProperty("novaSenha")
        @NotBlank(message = "A nova senha é obrigatória")
        String novaSenha
) {
    @JsonCreator
    public RedefinirSenhaRequest(@JsonProperty("token") String token,
                                 @JsonProperty("novaSenha") String novaSenha) {
        if (token != null) {
            this.token = token.trim();
        } else {
            this.token = null;
        }

        if (novaSenha != null) {
            this.novaSenha = novaSenha.trim();
        } else {
            this.novaSenha = null;
        }
    }
}
