package com.sistemajuridico.backend.presentation.openapi;

import com.sistemajuridico.backend.presentation.dtos.ErroPadraoDTO;
import com.sistemajuridico.backend.presentation.dtos.TribunalStatusDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Integrações Externas", description = "Monitoramento e integração com serviços judiciais e tribunais eletrônicos")
public interface IntegracaoTribunalControllerOpenApi {

    @Operation(summary = "Retorna o status atual de sincronização com os tribunais (ex: TJRS, TRF4, TRT4, STJ)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status das integrações de tribunais retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<TribunalStatusDTO> verificarStatusTribunais();

    @Operation(summary = "Sincroniza sob demanda as intimações do Comunica PJe para o advogado autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sincronização executada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Advogado sem OAB válida cadastrada",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não localizado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<com.sistemajuridico.backend.presentation.dtos.pje.SincronizacaoPjeResultadoDTO> sincronizarIntimacoesPje(java.security.Principal principal);
}
