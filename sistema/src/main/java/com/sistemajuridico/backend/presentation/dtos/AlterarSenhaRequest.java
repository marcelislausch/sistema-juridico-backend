package com.sistemajuridico.backend.presentation.dtos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record AlterarSenhaRequest(
        @JsonProperty("senhaAtual")
        @NotBlank(message = "A senha atual é obrigatória")
        String senhaAtual,

        @JsonProperty("novaSenha")
        @NotBlank(message = "A nova senha é obrigatória")
        String novaSenha
) {
    @JsonCreator
    public AlterarSenhaRequest(@JsonProperty("senhaAtual") String senhaAtual,
                               @JsonProperty("novaSenha") String novaSenha) {
        if (senhaAtual != null) {
            this.senhaAtual = senhaAtual.trim();
        } else {
            this.senhaAtual = null;
        }

        if (novaSenha != null) {
            this.novaSenha = novaSenha.trim();
        } else {
            this.novaSenha = null;
        }
    }
}
