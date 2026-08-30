package com.sistemajuridico.backend.presentation.dtos;

import com.sistemajuridico.backend.core.domain.Usuario;
import com.sistemajuridico.backend.core.domain.enums.PerfilAcessoEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UsuarioDTO(
        UUID id,

        @NotBlank(message = "O nome é obrigatório")
        String nome,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "Formato de e-mail inválido")
        String email,

        String senha,

        @NotNull(message = "O perfil de acesso é obrigatório")
        PerfilAcessoEnum perfil,

        String oab,

        boolean ativo
) {

    public Usuario toEntity() {
        Usuario usuario = new Usuario();
        usuario.setId(this.id());
        usuario.setNome(this.nome());
        usuario.setEmail(this.email());
        usuario.setSenhaHash(this.senha());
        usuario.setPerfil(this.perfil());
        usuario.setOab(this.oab());
        usuario.setAtivo(this.ativo());
        return usuario;
    }

    public static UsuarioDTO fromEntity(Usuario usuario) {
        return new UsuarioDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                null, // REGRA DE SEGURANÇA: nunca vazar o hash no JSON
                usuario.getPerfil(),
                usuario.getOab(),
                usuario.isAtivo()
        );
    }
}
