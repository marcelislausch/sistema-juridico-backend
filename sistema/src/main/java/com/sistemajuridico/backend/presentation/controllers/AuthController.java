package com.sistemajuridico.backend.presentation.controllers;

import com.sistemajuridico.backend.core.domain.Usuario;
import com.sistemajuridico.backend.core.domain.exceptions.RecursoNaoEncontradoException;
import com.sistemajuridico.backend.core.usecases.AutenticarUsuarioUseCase;
import com.sistemajuridico.backend.infrastructure.persistence.UsuarioRepository;
import com.sistemajuridico.backend.presentation.dtos.LoginDTO;
import com.sistemajuridico.backend.presentation.dtos.TokenDTO;
import com.sistemajuridico.backend.presentation.dtos.UsuarioDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AutenticarUsuarioUseCase autenticarUsuarioUseCase;
    private final UsuarioRepository usuarioRepository;

    public AuthController(AutenticarUsuarioUseCase autenticarUsuarioUseCase,
                          UsuarioRepository usuarioRepository) {
        this.autenticarUsuarioUseCase = autenticarUsuarioUseCase;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenDTO> login(@RequestBody @Valid LoginDTO dto) {
        String token = autenticarUsuarioUseCase.executar(dto);
        return ResponseEntity.ok(new TokenDTO(token));
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioDTO> me() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = principal != null ? principal.toString() : null;

        Optional<Usuario> optUsuario = usuarioRepository.findByEmail(email);
        if (optUsuario.isEmpty()) {
            throw new RecursoNaoEncontradoException("Usuário não encontrado");
        }

        Usuario usuario = optUsuario.get();
        return ResponseEntity.ok(UsuarioDTO.fromEntity(usuario));
    }
}
