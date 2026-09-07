package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Usuario;
import com.sistemajuridico.backend.core.domain.exceptions.RegraNegocioException;
import com.sistemajuridico.backend.infrastructure.persistence.UsuarioRepository;
import com.sistemajuridico.backend.presentation.dtos.UsuarioDTO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CadastrarUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public CadastrarUsuarioUseCase(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario executar(Usuario usuario) {
        Optional<Usuario> busca = usuarioRepository.findByEmail(usuario.getEmail());
        if (busca.isPresent()) {
            throw new RegraNegocioException("E-mail já cadastrado no sistema!");
        }

        if (usuario.getOab() == null || usuario.getOab().trim().isEmpty()) {
            usuario.setOab(null);
        } else {
            usuario.setOab(usuario.getOab().trim());
        }

        String senhaCrip = passwordEncoder.encode(usuario.getSenhaHash());
        usuario.setSenhaHash(senhaCrip);

        return usuarioRepository.save(usuario);
    }

    public Usuario executar(UsuarioDTO dto) {
        Usuario usuario = dto.toEntity();
        if (dto.oab() == null || dto.oab().trim().isEmpty()) {
            usuario.setOab(null);
        } else {
            usuario.setOab(dto.oab().trim());
        }
        return executar(usuario);
    }
}
