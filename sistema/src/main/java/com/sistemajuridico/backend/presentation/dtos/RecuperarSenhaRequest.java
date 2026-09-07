package com.sistemajuridico.backend.presentation.dtos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RecuperarSenhaRequest(
        @JsonProperty("email")
        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "Formato de e-mail inválido")
        String email
) {
    @JsonCreator
    public RecuperarSenhaRequest(@JsonProperty("email") String email) {
        if (email != null) {
            this.email = email.trim();
        } else {
            this.email = null;
        }
    }
}
