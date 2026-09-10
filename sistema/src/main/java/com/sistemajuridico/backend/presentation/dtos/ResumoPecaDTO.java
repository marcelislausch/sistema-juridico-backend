package com.sistemajuridico.backend.presentation.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "Payload para requisição de resumo preparatório de audiência a partir de documentos dos autos")
public record ResumoPecaDTO(
        @Schema(description = "Lista de identificadores dos documentos (UUID do sistema ou ID no Google Drive) a serem lidos e sumarizados pela IA",
                example = "[\"1PPOtwVobdFDcWgy52WS7rpy0pehRiJAZ\", \"a2d5926a-939e-4c7b-94ec-7cfa8e1b1234\"]")
        @NotEmpty(message = "A lista de IDs de documentos é obrigatória para gerar o resumo")
        List<String> documentosIds
) {
}

