package com.sistemajuridico.backend.presentation.controllers;

import com.sistemajuridico.backend.core.domain.Usuario;
import com.sistemajuridico.backend.core.usecases.CadastrarUsuarioUseCase;
import com.sistemajuridico.backend.core.usecases.ListarAdvogadosUseCase;
import com.sistemajuridico.backend.presentation.dtos.UsuarioDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final CadastrarUsuarioUseCase cadastrarUsuarioUseCase;
    private final ListarAdvogadosUseCase listarAdvogadosUseCase;

    public UsuarioController(CadastrarUsuarioUseCase cadastrarUsuarioUseCase,
                             ListarAdvogadosUseCase listarAdvogadosUseCase) {
        this.cadastrarUsuarioUseCase = cadastrarUsuarioUseCase;
        this.listarAdvogadosUseCase = listarAdvogadosUseCase;
    }

    @PostMapping
    public ResponseEntity<UsuarioDTO> criar(@RequestBody UsuarioDTO dto) {
        Usuario usuario = dto.toEntity();
        Usuario usuarioSalvo = cadastrarUsuarioUseCase.executar(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioDTO.fromEntity(usuarioSalvo));
    }

    @GetMapping("/advogados")
    public ResponseEntity<List<UsuarioDTO>> listarAdvogados() {
        List<Usuario> advogados = listarAdvogadosUseCase.executar();
        List<UsuarioDTO> response = advogados.stream()
                .map(UsuarioDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }
}
