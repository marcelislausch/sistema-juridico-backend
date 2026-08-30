package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Usuario;
import com.sistemajuridico.backend.core.domain.exceptions.RegraNegocioException;
import com.sistemajuridico.backend.infrastructure.persistence.UsuarioRepository;
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

        String senhaCrip = passwordEncoder.encode(usuario.getSenhaHash());
        usuario.setSenhaHash(senhaCrip);

        return usuarioRepository.save(usuario);
    }
}
