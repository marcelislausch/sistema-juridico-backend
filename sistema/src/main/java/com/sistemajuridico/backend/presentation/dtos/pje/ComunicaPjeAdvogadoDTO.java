package com.sistemajuridico.backend.presentation.dtos.pje;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ComunicaPjeAdvogadoDTO(
        Long id,
        String nome,
        @JsonProperty("numero_oab")
        String numeroOab,
        @JsonProperty("uf_oab")
        String ufOab
) {}
