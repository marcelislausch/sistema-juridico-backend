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

@Tag(name = "IntegraÃ§Ãµes Externas", description = "Monitoramento e integraÃ§Ã£o com serviÃ§os judiciais e tribunais eletrÃ´nicos")
public interface IntegracaoTribunalControllerOpenApi {

    @Operation(summary = "Retorna o status atual de sincronizaÃ§Ã£o com os tribunais (ex: TJRS, TRF4, TRT4, STJ)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status das integraÃ§Ãµes de tribunais retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "NÃ£o autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<TribunalStatusDTO> verificarStatusTribunais();
}
