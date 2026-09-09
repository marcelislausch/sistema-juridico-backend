package com.sistemajuridico.backend.presentation.dtos;

import com.sistemajuridico.backend.core.domain.Usuario;
import com.sistemajuridico.backend.core.domain.enums.PerfilAcessoEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AtualizarUsuarioRequest(
        @NotBlank(message = "O nome é obrigatório")
        String nome,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "Formato de e-mail inválido")
        String email,

        @NotNull(message = "O perfil de acesso é obrigatório")
        PerfilAcessoEnum perfil,

        String oab,

        Boolean ativo
) {

    public void aplicarEm(Usuario usuario) {
        usuario.setNome(this.nome());
        usuario.setEmail(this.email());
        usuario.setPerfil(this.perfil());
        if (this.oab() == null || this.oab().trim().isEmpty()) {
            usuario.setOab(null);
        } else {
            usuario.setOab(this.oab().trim());
        }
        if (this.ativo() != null) {
            usuario.setAtivo(this.ativo());
        }
    }
}
