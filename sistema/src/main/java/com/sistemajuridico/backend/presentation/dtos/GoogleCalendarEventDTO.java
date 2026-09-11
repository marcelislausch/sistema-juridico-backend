package com.sistemajuridico.backend.presentation.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.UUID;

public record GoogleCalendarEventDTO(
        @NotBlank(message = "O ID do evento no Google é obrigatório")
        @JsonAlias({"id", "eventId"})
        String googleEventId,

        @JsonAlias({"summary", "titulo"})
        String descricao,

        @JsonAlias({"data", "dataEvento"})
        LocalDate dataVencimento,

        String status,

        UUID usuarioId,

        String usuarioEmail
) {
    public GoogleCalendarEventDTO(String googleEventId, String descricao, LocalDate dataVencimento, String status) {
        this(googleEventId, descricao, dataVencimento, status, null, null);
    }
}
