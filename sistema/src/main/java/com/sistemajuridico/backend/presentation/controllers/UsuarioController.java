package com.sistemajuridico.backend.presentation.controllers;

import com.sistemajuridico.backend.core.domain.Usuario;
import com.sistemajuridico.backend.core.usecases.CadastrarUsuarioUseCase;
import com.sistemajuridico.backend.core.usecases.ListarAdvogadosUseCase;
import com.sistemajuridico.backend.presentation.dtos.UsuarioDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<UsuarioDTO> criar(@RequestBody @Valid UsuarioDTO dto) {
        Usuario usuario = dto.toEntity();
        Usuario usuarioSalvo = cadastrarUsuarioUseCase.executar(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioDTO.fromEntity(usuarioSalvo));
    }

    @GetMapping("/advogados")
    public ResponseEntity<List<UsuarioDTO>> listarAdvogados() {
        List<Usuario> advogados = listarAdvogadosUseCase.executar();
        List<UsuarioDTO> response = new ArrayList<>();
        for (Usuario advogado : advogados) {
            response.add(UsuarioDTO.fromEntity(advogado));
        }
        return ResponseEntity.ok(response);
    }
}
