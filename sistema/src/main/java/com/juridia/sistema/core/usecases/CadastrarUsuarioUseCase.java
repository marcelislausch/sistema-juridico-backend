package com.juridia.sistema.core.usecases;

import com.juridia.sistema.core.domain.Usuario;
import com.juridia.sistema.infrastructure.persistence.UsuarioRepository;
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
        return usuarioRepository.save(usuario);
    }
}
