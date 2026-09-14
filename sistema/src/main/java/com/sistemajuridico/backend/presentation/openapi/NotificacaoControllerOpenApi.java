package com.sistemajuridico.backend.presentation.openapi;

import com.sistemajuridico.backend.presentation.dtos.ErroPadraoDTO;
import com.sistemajuridico.backend.presentation.dtos.NotificacaoResumoDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.time.LocalDate;

@Tag(name = "Notificações", description = "Central de notificações operacionais unificadas do escritório")
public interface NotificacaoControllerOpenApi {

    @Operation(summary = "Obtém as notificações operacionais ativas para a data informada a partir do token de sessão")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resumo de notificações retornado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de consulta inválidos",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuário da sessão não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "422", description = "Regra de negócio violada",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<NotificacaoResumoDTO> obterResumo(LocalDate data, Principal principal);
}
