package com.sistemajuridico.backend.presentation.controllers;

import com.sistemajuridico.backend.core.domain.Usuario;
import com.sistemajuridico.backend.core.domain.exceptions.RecursoNaoEncontradoException;
import com.sistemajuridico.backend.core.domain.exceptions.RegraNegocioException;
import com.sistemajuridico.backend.core.usecases.DashboardAdvogadoUseCase;
import com.sistemajuridico.backend.infrastructure.persistence.UsuarioRepository;
import com.sistemajuridico.backend.presentation.dtos.ErroPadraoDTO;
import com.sistemajuridico.backend.presentation.dtos.ResumoDashboardDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Métricas consolidadas, contadores e visão geral do advogado")
public class DashboardController {

    private final DashboardAdvogadoUseCase dashboardAdvogadoUseCase;
    private final UsuarioRepository usuarioRepository;

    public DashboardController(DashboardAdvogadoUseCase dashboardAdvogadoUseCase,
                               UsuarioRepository usuarioRepository) {
        this.dashboardAdvogadoUseCase = dashboardAdvogadoUseCase;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    @Operation(summary = "Obter dados do dashboard autenticado", description = "Recupera os contadores, audiências e tarefas do usuário atualmente autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Métricas do dashboard recuperadas com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuário da sessão não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "422", description = "Regra de negócio violada",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    public ResponseEntity<ResumoDashboardDTO> obterDashboardAutenticado(Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new RegraNegocioException("Usuário não autenticado no sistema!");
        }

        String email = principal.getName();
        Optional<Usuario> optUsuario = this.usuarioRepository.findByEmail(email);
        if (optUsuario.isEmpty()) {
            throw new RecursoNaoEncontradoException("Usuário autenticado não encontrado no sistema!");
        }

        UUID usuarioId = optUsuario.get().getId();
        ResumoDashboardDTO resumo = this.dashboardAdvogadoUseCase.executar(usuarioId);
        return ResponseEntity.ok(resumo);
    }

    @GetMapping("/{usuarioId}")
    @Operation(summary = "Obter dashboard por ID de usuário", description = "Recupera as métricas e indicadores de um usuário/advogado específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard recuperado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    public ResponseEntity<ResumoDashboardDTO> obterDashboard(@PathVariable UUID usuarioId) {
        ResumoDashboardDTO resumo = this.dashboardAdvogadoUseCase.executar(usuarioId);
        return ResponseEntity.ok(resumo);
    }
}
