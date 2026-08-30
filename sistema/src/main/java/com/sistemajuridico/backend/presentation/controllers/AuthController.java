package com.sistemajuridico.backend.presentation.controllers;

import com.sistemajuridico.backend.core.usecases.AutenticarUsuarioUseCase;
import com.sistemajuridico.backend.presentation.dtos.LoginDTO;
import com.sistemajuridico.backend.presentation.dtos.TokenDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AutenticarUsuarioUseCase autenticarUsuarioUseCase;

    public AuthController(AutenticarUsuarioUseCase autenticarUsuarioUseCase) {
        this.autenticarUsuarioUseCase = autenticarUsuarioUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenDTO> login(@RequestBody @Valid LoginDTO dto) {
        String token = autenticarUsuarioUseCase.executar(dto.email(), dto.senha());
        return ResponseEntity.ok(new TokenDTO(token));
    }
}
