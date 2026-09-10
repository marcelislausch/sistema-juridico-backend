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

@Tag(name = "Dashboard", description = "MÃ©tricas consolidadas, contadores e visÃ£o geral do advogado")
public interface DashboardControllerOpenApi {

    @Operation(summary = "Obter dados do dashboard autenticado", description = "Recupera os contadores, audiÃªncias e tarefas do usuÃ¡rio atualmente autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "MÃ©tricas do dashboard recuperadas com sucesso"),
            @ApiResponse(responseCode = "401", description = "NÃ£o autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "UsuÃ¡rio da sessÃ£o nÃ£o encontrado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "422", description = "Regra de negÃ³cio violada",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<ResumoDashboardDTO> obterDashboardAutenticado(Principal principal);

    @Operation(summary = "Obter dashboard por ID de usuÃ¡rio", description = "Recupera as mÃ©tricas e indicadores de um usuÃ¡rio/advogado especÃ­fico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard recuperado com sucesso"),
            @ApiResponse(responseCode = "401", description = "NÃ£o autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "UsuÃ¡rio nÃ£o encontrado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<ResumoDashboardDTO> obterDashboard(UUID usuarioId);
}
