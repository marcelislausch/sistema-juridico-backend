package com.sistemajuridico.backend.presentation.dtos;

import com.sistemajuridico.backend.core.domain.Usuario;
import com.sistemajuridico.backend.core.domain.enums.PerfilAcessoEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CriarUsuarioRequest(
        @NotBlank(message = "O nome é obrigatório")
        String nome,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "Formato de e-mail inválido")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 6, message = "A senha deve conter no mínimo 6 caracteres")
        String senha,

        @NotNull(message = "O perfil de acesso é obrigatório")
        PerfilAcessoEnum perfil,

        String oab
) {

    public Usuario toEntity() {
        Usuario usuario = new Usuario();
        usuario.setNome(this.nome());
        usuario.setEmail(this.email());
        usuario.setSenhaHash(this.senha());
        usuario.setPerfil(this.perfil());
        if (this.oab() == null || this.oab().trim().isEmpty()) {
            usuario.setOab(null);
        } else {
            usuario.setOab(this.oab().trim());
        }
        usuario.setAtivo(true);
        return usuario;
    }
}
