package com.sistemajuridico.backend.presentation.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sistemajuridico.backend.core.usecases.SincronizarEventoGoogleCalendarUseCase;
import com.sistemajuridico.backend.infrastructure.security.GoogleOAuthTokenManager;
import com.sistemajuridico.backend.presentation.dtos.GoogleCalendarEventDTO;
import com.sistemajuridico.backend.presentation.openapi.GoogleCalendarWebhookControllerOpenApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/integracoes/google-calendar")
public class GoogleCalendarWebhookController implements GoogleCalendarWebhookControllerOpenApi {

    private static final Logger log = LoggerFactory.getLogger(GoogleCalendarWebhookController.class);

    private final SincronizarEventoGoogleCalendarUseCase sincronizarEventoGoogleCalendarUseCase;
    private final GoogleOAuthTokenManager googleOAuthTokenManager;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public GoogleCalendarWebhookController(SincronizarEventoGoogleCalendarUseCase sincronizarEventoGoogleCalendarUseCase,
                                           GoogleOAuthTokenManager googleOAuthTokenManager) {
        this.sincronizarEventoGoogleCalendarUseCase = sincronizarEventoGoogleCalendarUseCase;
        this.googleOAuthTokenManager = googleOAuthTokenManager;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public GoogleCalendarWebhookController(SincronizarEventoGoogleCalendarUseCase sincronizarEventoGoogleCalendarUseCase,
                                           GoogleOAuthTokenManager googleOAuthTokenManager,
                                           RestTemplate restTemplate,
                                           ObjectMapper objectMapper) {
        this.sincronizarEventoGoogleCalendarUseCase = sincronizarEventoGoogleCalendarUseCase;
        this.googleOAuthTokenManager = googleOAuthTokenManager;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
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

        // Se o payload vier diretamente no corpo da requisição (ex: testes manuais/Swagger)
        if (dto != null && dto.googleEventId() != null && !dto.googleEventId().trim().isEmpty()) {
            this.sincronizarEventoGoogleCalendarUseCase.executar(dto);
            return ResponseEntity.ok().build();
        }

        // Padrão Thin Payload do Google Calendar: busca os dados atualizados via Google Calendar API
        try {
            String updatedMin = Instant.now().minusSeconds(300).toString();
            String url = "https://www.googleapis.com/calendar/v3/calendars/primary/events?singleEvents=true&showDeleted=true&updatedMin=" + updatedMin;

            String token = this.googleOAuthTokenManager.obterAccessToken();

            if (token == null || token.trim().isEmpty()) {
                log.warn("Token de acesso do Google Calendar não disponível para sincronização");
                return ResponseEntity.ok().build();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
            ResponseEntity<String> response = this.restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode rootNode = this.objectMapper.readTree(response.getBody());
                JsonNode itemsNode = rootNode.get("items");

                if (itemsNode != null && itemsNode.isArray()) {
                    for (int i = 0; i < itemsNode.size(); i++) {
                        JsonNode itemNode = itemsNode.get(i);

                        String id = null;
                        JsonNode idNode = itemNode.get("id");
                        if (idNode != null && !idNode.isNull()) {
                            id = idNode.asText();
                        }

                        String summary = null;
                        JsonNode summaryNode = itemNode.get("summary");
                        if (summaryNode != null && !summaryNode.isNull()) {
                            summary = summaryNode.asText();
                        }

                        String status = null;
                        JsonNode statusNode = itemNode.get("status");
                        if (statusNode != null && !statusNode.isNull()) {
                            status = statusNode.asText();
                        }

                        LocalDate dataVencimento = extrairDataVencimento(itemNode.get("start"));

                        if (id != null && !id.trim().isEmpty()) {
                            String descricao = summary;
                            if (descricao == null || descricao.trim().isEmpty()) {
                                descricao = "Compromisso Google Calendar";
                            }

                            GoogleCalendarEventDTO eventDTO = new GoogleCalendarEventDTO(
                                    id.trim(),
                                    descricao.trim(),
                                    dataVencimento,
                                    status
                            );
                            this.sincronizarEventoGoogleCalendarUseCase.executar(eventDTO);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Erro ao sincronizar evento via API do Google Calendar: {}", e.getMessage(), e);
        }

        // Retorno 200 OK mandatório para que o Google não reenvie a notificação
        return ResponseEntity.ok().build();
    }

    private LocalDate extrairDataVencimento(JsonNode startNode) {
        if (startNode == null || startNode.isNull()) {
            return LocalDate.now();
        }

        JsonNode dateTimeNode = startNode.get("dateTime");
        if (dateTimeNode != null && !dateTimeNode.isNull() && !dateTimeNode.asText().trim().isEmpty()) {
            String dtText = dateTimeNode.asText().trim();
            try {
                return OffsetDateTime.parse(dtText).toLocalDate();
            } catch (Exception ignored) {
            }

            try {
                return LocalDateTime.parse(dtText).toLocalDate();
            } catch (Exception ignored) {
            }

            try {
                if (dtText.length() >= 10) {
                    return LocalDate.parse(dtText.substring(0, 10));
                }
            } catch (Exception ignored) {
            }
        }

        JsonNode dateNode = startNode.get("date");
        if (dateNode != null && !dateNode.isNull() && !dateNode.asText().trim().isEmpty()) {
            String dText = dateNode.asText().trim();
            try {
                return LocalDate.parse(dText);
            } catch (Exception ignored) {
            }
        }

        return LocalDate.now();
    }
}
