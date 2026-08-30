package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Usuario;
import com.sistemajuridico.backend.core.domain.exceptions.RegraNegocioException;
import com.sistemajuridico.backend.infrastructure.persistence.UsuarioRepository;
import com.sistemajuridico.backend.infrastructure.security.TokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AutenticarUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AutenticarUsuarioUseCase(UsuarioRepository usuarioRepository,
                                    PasswordEncoder passwordEncoder,
                                    TokenService tokenService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    public String executar(String email, String senha) {
        Optional<Usuario> optUsuario = usuarioRepository.findByEmail(email);
        if (optUsuario.isEmpty()) {
            throw new RegraNegocioException("Credenciais inválidas");
        }

        Usuario usuario = optUsuario.get();

        if (!passwordEncoder.matches(senha, usuario.getSenhaHash())) {
            throw new RegraNegocioException("Credenciais inválidas");
        }

        return tokenService.gerarToken(usuario);
    }
}
