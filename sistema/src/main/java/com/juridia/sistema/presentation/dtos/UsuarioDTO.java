package com.juridia.sistema.presentation.dtos;

import com.juridia.sistema.core.domain.Usuario;
import com.juridia.sistema.core.domain.enums.PerfilAcesso;

import java.util.UUID;

public record UsuarioDTO(
        UUID id,
        String nome,
        String email,
        String senha,
        PerfilAcesso perfil,
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
