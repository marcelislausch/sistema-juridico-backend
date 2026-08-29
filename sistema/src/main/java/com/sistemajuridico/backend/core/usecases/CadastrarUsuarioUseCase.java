package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Usuario;
import com.sistemajuridico.backend.infrastructure.persistence.UsuarioRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CadastrarUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;

    public CadastrarUsuarioUseCase(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario executar(Usuario usuario) {
        Optional<Usuario> busca = usuarioRepository.findByEmail(usuario.getEmail());
        if (busca.isPresent()) {
            throw new RuntimeException("E-mail já cadastrado!");
        }

        String senhaCrip = new BCryptPasswordEncoder().encode(usuario.getSenhaHash());
        usuario.setSenhaHash(senhaCrip);

        return usuarioRepository.save(usuario);
    }
}

