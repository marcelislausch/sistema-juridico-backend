package com.sistemajuridico.backend.presentation.controllers;

import com.sistemajuridico.backend.core.domain.Usuario;
import com.sistemajuridico.backend.core.usecases.BuscarUsuarioPorIdUseCase;
import com.sistemajuridico.backend.core.usecases.CadastrarUsuarioUseCase;
import com.sistemajuridico.backend.core.usecases.ListarAdvogadosUseCase;
import com.sistemajuridico.backend.core.usecases.ListarUsuariosUseCase;
import com.sistemajuridico.backend.presentation.dtos.CriarUsuarioRequest;
import com.sistemajuridico.backend.presentation.dtos.UsuarioResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuários", description = "Gestão de equipe, perfis de acesso e credenciais")
public class UsuarioController {

    private final CadastrarUsuarioUseCase cadastrarUsuarioUseCase;
    private final ListarAdvogadosUseCase listarAdvogadosUseCase;
    private final BuscarUsuarioPorIdUseCase buscarUsuarioPorIdUseCase;
    private final ListarUsuariosUseCase listarUsuariosUseCase;

    public UsuarioController(CadastrarUsuarioUseCase cadastrarUsuarioUseCase,
                             ListarAdvogadosUseCase listarAdvogadosUseCase,
                             BuscarUsuarioPorIdUseCase buscarUsuarioPorIdUseCase,
                             ListarUsuariosUseCase listarUsuariosUseCase) {
        this.cadastrarUsuarioUseCase = cadastrarUsuarioUseCase;
        this.listarAdvogadosUseCase = listarAdvogadosUseCase;
        this.buscarUsuarioPorIdUseCase = buscarUsuarioPorIdUseCase;
        this.listarUsuariosUseCase = listarUsuariosUseCase;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ADVOGADO')")
    @Operation(summary = "Listagem paginada dos membros da equipe com suporte a filtros")
    public ResponseEntity<Page<UsuarioResponseDTO>> listar(
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(name = "q", required = false) String q,
            @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        Page<Usuario> pagina = this.listarUsuariosUseCase.executar(ativo, q, pageable);
        List<UsuarioResponseDTO> dtos = new ArrayList<>();
        for (Usuario u : pagina.getContent()) {
            dtos.add(UsuarioResponseDTO.fromEntity(u));
        }
        Page<UsuarioResponseDTO> response = new PageImpl<>(dtos, pageable, pagina.getTotalElements());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ADVOGADO')")
    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> criar(@RequestBody @Valid CriarUsuarioRequest request) {
        Usuario usuario = request.toEntity();
        Usuario usuarioSalvo = cadastrarUsuarioUseCase.executar(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponseDTO.fromEntity(usuarioSalvo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable UUID id) {
        Usuario usuario = this.buscarUsuarioPorIdUseCase.executar(id);
        return ResponseEntity.ok(UsuarioResponseDTO.fromEntity(usuario));
    }

    @GetMapping("/advogados")
    public ResponseEntity<List<UsuarioResponseDTO>> listarAdvogados() {
        List<Usuario> advogados = listarAdvogadosUseCase.executar();
        List<UsuarioResponseDTO> response = new ArrayList<>();
        for (Usuario advogado : advogados) {
            response.add(UsuarioResponseDTO.fromEntity(advogado));
        }
        return ResponseEntity.ok(response);
    }
}
