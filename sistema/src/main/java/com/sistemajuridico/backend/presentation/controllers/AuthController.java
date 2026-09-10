package com.sistemajuridico.backend.presentation.controllers;

import com.sistemajuridico.backend.presentation.openapi.AuthControllerOpenApi;

import com.sistemajuridico.backend.core.domain.Usuario;
import com.sistemajuridico.backend.core.domain.exceptions.RecursoNaoEncontradoException;
import com.sistemajuridico.backend.core.service.AuthService;
import com.sistemajuridico.backend.core.usecases.AutenticarUsuarioUseCase;
import com.sistemajuridico.backend.infrastructure.persistence.UsuarioRepository;
import com.sistemajuridico.backend.presentation.dtos.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController implements AuthControllerOpenApi {

    private final AutenticarUsuarioUseCase autenticarUsuarioUseCase;
    private final UsuarioRepository usuarioRepository;
    private final AuthService authService;

    public AuthController(AutenticarUsuarioUseCase autenticarUsuarioUseCase,
                          UsuarioRepository usuarioRepository,
                          AuthService authService) {
        this.autenticarUsuarioUseCase = autenticarUsuarioUseCase;
        this.usuarioRepository = usuarioRepository;
        this.authService = authService;
    }

    @Override
    @PostMapping("/login")
    public ResponseEntity<TokenDTO> login(@RequestBody @Valid LoginDTO dto) {
        String token = autenticarUsuarioUseCase.executar(dto);
        return ResponseEntity.ok(new TokenDTO(token));
    }

    @Override
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> me() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = null;
        if (principal != null) {
            email = principal.toString();
        }

        Optional<Usuario> optUsuario = usuarioRepository.findByEmail(email);
        if (optUsuario.isEmpty()) {
            throw new RecursoNaoEncontradoException("Usuário não encontrado");
        }

        Usuario usuario = optUsuario.get();
        return ResponseEntity.ok(UsuarioResponseDTO.fromEntity(usuario));
    }

    @Override
    @PostMapping("/recuperar-senha")
    public ResponseEntity<Map<String, String>> recuperarSenha(@RequestBody @Valid RecuperarSenhaRequest request) {
        this.authService.solicitarRecuperacaoSenha(request.email());
        Map<String, String> resposta = new HashMap<>();
        resposta.put("mensagem", "Se a conta existir, as instruções serão enviadas.");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(resposta);
    }

    @Override
    @PostMapping("/redefinir-senha")
    public ResponseEntity<Map<String, String>> redefinirSenha(@RequestBody @Valid RedefinirSenhaRequest request) {
        this.authService.redefinirSenha(request.token(), request.novaSenha());
        Map<String, String> resposta = new HashMap<>();
        resposta.put("mensagem", "Senha redefinida com sucesso.");
        return ResponseEntity.ok(resposta);
    }

    @Override
    @PatchMapping("/me/senha")
    public ResponseEntity<Void> alterarSenha(@RequestBody @Valid AlterarSenhaRequest request) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = null;
        if (principal != null) {
            email = principal.toString();
        }

        this.authService.alterarSenhaAutenticada(email, request.senhaAtual(), request.novaSenha());
        return ResponseEntity.noContent().build();
    }
}
