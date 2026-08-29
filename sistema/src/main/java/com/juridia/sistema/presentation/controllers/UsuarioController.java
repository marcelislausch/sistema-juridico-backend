package com.juridia.sistema.presentation.controllers;

import com.juridia.sistema.core.domain.Usuario;
import com.juridia.sistema.core.usecases.CadastrarUsuarioUseCase;
import com.juridia.sistema.presentation.dtos.UsuarioDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final CadastrarUsuarioUseCase cadastrarUsuarioUseCase;

    public UsuarioController(CadastrarUsuarioUseCase cadastrarUsuarioUseCase) {
        this.cadastrarUsuarioUseCase = cadastrarUsuarioUseCase;
    }

    @PostMapping
    public ResponseEntity<UsuarioDTO> criar(@RequestBody UsuarioDTO dto) {
        Usuario usuario = dto.toEntity();
        Usuario usuarioSalvo = cadastrarUsuarioUseCase.executar(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioDTO.fromEntity(usuarioSalvo));
    }
}
