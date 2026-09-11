package com.sistemajuridico.backend.presentation.controllers;

import com.sistemajuridico.backend.core.usecases.SincronizarEventoGoogleCalendarUseCase;
import com.sistemajuridico.backend.presentation.dtos.GoogleCalendarEventDTO;
import com.sistemajuridico.backend.presentation.openapi.GoogleCalendarWebhookControllerOpenApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/integracoes/google-calendar")
public class GoogleCalendarWebhookController implements GoogleCalendarWebhookControllerOpenApi {

    private static final Logger log = LoggerFactory.getLogger(GoogleCalendarWebhookController.class);

    private final SincronizarEventoGoogleCalendarUseCase sincronizarEventoGoogleCalendarUseCase;

    public GoogleCalendarWebhookController(SincronizarEventoGoogleCalendarUseCase sincronizarEventoGoogleCalendarUseCase) {
        this.sincronizarEventoGoogleCalendarUseCase = sincronizarEventoGoogleCalendarUseCase;
    }

    @Override
    @PostMapping("/webhook")
    public ResponseEntity<Void> receberWebhook(
            @RequestHeader(value = "X-Goog-Channel-ID", required = false) String channelId,
            @RequestHeader(value = "X-Goog-Resource-State", required = false) String resourceState,
            @RequestHeader(value = "X-Goog-Resource-ID", required = false) String resourceId,
            @RequestBody(required = false) GoogleCalendarEventDTO dto
    ) {
        log.info("Webhook recebido do Google Calendar - Channel: {}, State: {}, Resource: {}", channelId, resourceState, resourceId);

        // Se for uma notificação de sincronização de canal (handshake sync do Google), confirma com 200 OK
        if (resourceState != null && resourceState.equalsIgnoreCase("sync")) {
            return ResponseEntity.ok().build();
        }

        if (dto != null && dto.googleEventId() != null && !dto.googleEventId().trim().isEmpty()) {
            this.sincronizarEventoGoogleCalendarUseCase.executar(dto);
        } else if (resourceId != null && !resourceId.trim().isEmpty()) {
            String status = "confirmed";
            if (resourceState != null && (resourceState.equalsIgnoreCase("not_exists") || resourceState.equalsIgnoreCase("trash"))) {
                status = "cancelled";
            }
            GoogleCalendarEventDTO fallbackDto = new GoogleCalendarEventDTO(
                    resourceId.trim(),
                    "Compromisso Google Calendar",
                    LocalDate.now(),
                    status
            );
            this.sincronizarEventoGoogleCalendarUseCase.executar(fallbackDto);
        }

        // Retorno 200 OK mandatório para que o Google não reenvie a notificação
        return ResponseEntity.ok().build();
    }
}
