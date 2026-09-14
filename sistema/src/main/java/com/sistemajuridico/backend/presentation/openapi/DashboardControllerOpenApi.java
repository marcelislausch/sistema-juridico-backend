package com.sistemajuridico.backend.presentation.openapi;

import com.sistemajuridico.backend.presentation.dtos.ErroPadraoDTO;
import com.sistemajuridico.backend.presentation.dtos.ResumoDashboardDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.util.UUID;

@Tag(name = "Dashboard", description = "Métricas consolidadas, contadores e visão geral do advogado")
public interface DashboardControllerOpenApi {

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
    ResponseEntity<ResumoDashboardDTO> obterDashboardAutenticado(Principal principal);

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
    ResponseEntity<ResumoDashboardDTO> obterDashboard(UUID usuarioId);
}
