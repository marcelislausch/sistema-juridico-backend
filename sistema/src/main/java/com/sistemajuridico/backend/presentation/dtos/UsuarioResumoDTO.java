package com.sistemajuridico.backend.presentation.dtos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.sistemajuridico.backend.core.domain.Usuario;
import com.sistemajuridico.backend.core.domain.enums.PerfilAcessoEnum;

import java.util.UUID;

public record UsuarioResumoDTO(
        UUID id,
        String nome,
        String email,
        String oab,
        PerfilAcessoEnum perfil
) {

    public UsuarioResumoDTO(UUID id) {
        this(id, null, null, null, null);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static UsuarioResumoDTO fromId(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        return new UsuarioResumoDTO(UUID.fromString(id));
    }

    public static UsuarioResumoDTO fromEntity(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        return new UsuarioResumoDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getOab(),
                usuario.getPerfil()
        );
    }
}
