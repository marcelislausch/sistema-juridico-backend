package com.sistemajuridico.backend.presentation.controllers;

import com.sistemajuridico.backend.presentation.openapi.UsuarioControllerOpenApi;

import com.sistemajuridico.backend.core.domain.Usuario;
import com.sistemajuridico.backend.core.usecases.BuscarUsuarioPorIdUseCase;
import com.sistemajuridico.backend.core.usecases.CadastrarUsuarioUseCase;
import com.sistemajuridico.backend.core.usecases.ListarAdvogadosUseCase;
import com.sistemajuridico.backend.core.usecases.ListarUsuariosUseCase;
import com.sistemajuridico.backend.presentation.dtos.CriarUsuarioRequest;
import com.sistemajuridico.backend.presentation.dtos.UsuarioPageResponse;
import com.sistemajuridico.backend.presentation.dtos.UsuarioResponseDTO;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController implements UsuarioControllerOpenApi {

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

    @Override
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'ADVOGADO')")
    public ResponseEntity<UsuarioPageResponse> listar(
            @RequestParam(name = "ativo", required = false) Boolean ativo,
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "termoBusca", required = false) String termoBusca,
            @RequestParam(name = "termo", required = false) String termoParam,
            @ParameterObject @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        String termo = null;
        if (q != null && !q.trim().isEmpty()) {
            termo = q.trim();
        } else if (termoBusca != null && !termoBusca.trim().isEmpty()) {
            termo = termoBusca.trim();
        } else if (termoParam != null && !termoParam.trim().isEmpty()) {
            termo = termoParam.trim();
        }

        Page<Usuario> pagina = this.listarUsuariosUseCase.executar(ativo, termo, pageable);
        List<UsuarioResponseDTO> dtos = new ArrayList<>();
        for (Usuario u : pagina.getContent()) {
            dtos.add(UsuarioResponseDTO.fromEntity(u));
        }
        Page<UsuarioResponseDTO> response = new PageImpl<>(dtos, pageable, pagina.getTotalElements());
        return ResponseEntity.ok(UsuarioPageResponse.from(response));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'ADVOGADO')")
    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> criar(@RequestBody @Valid CriarUsuarioRequest request) {
        Usuario usuario = request.toEntity();
        Usuario usuarioSalvo = cadastrarUsuarioUseCase.executar(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponseDTO.fromEntity(usuarioSalvo));
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable UUID id) {
        Usuario usuario = this.buscarUsuarioPorIdUseCase.executar(id);
        return ResponseEntity.ok(UsuarioResponseDTO.fromEntity(usuario));
    }

    @Override
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
