package com.sistemajuridico.backend.presentation.dtos.pje;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ComunicaPjeDestinatarioAdvogadoDTO(
        Long id,
        @JsonProperty("comunicacao_id")
        Long comunicacaoId,
        @JsonProperty("advogado_id")
        Long advogadoId,
        ComunicaPjeAdvogadoDTO advogado
) {}
