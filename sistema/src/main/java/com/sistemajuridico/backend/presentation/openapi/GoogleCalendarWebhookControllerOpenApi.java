package com.sistemajuridico.backend.presentation.openapi;

import com.sistemajuridico.backend.presentation.dtos.ErroPadraoDTO;
import com.sistemajuridico.backend.presentation.dtos.GoogleCalendarEventDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Integrações", description = "Endpoints de integração com serviços externos e tribunais")
public interface GoogleCalendarWebhookControllerOpenApi {

    @Operation(summary = "Webhook do Google Calendar", description = "Recebe notificações push de eventos e compromissos originados no Google Calendar para sincronização unidirecional no sistema jurídico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notificação processada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Payload ou cabeçalhos inválidos",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno ao processar sincronização",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<Void> receberWebhook(
            String channelId,
            String resourceState,
            String resourceId,
            GoogleCalendarEventDTO dto
    );
}
