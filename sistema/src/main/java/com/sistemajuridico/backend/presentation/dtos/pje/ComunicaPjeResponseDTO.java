package com.sistemajuridico.backend.presentation.dtos.pje;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ComunicaPjeResponseDTO(
        String status,
        String message,
        Integer count,
        List<ComunicaPjeItemDTO> items
) {
    public ComunicaPjeResponseDTO {
        if (items == null) {
            items = new ArrayList<>();
        }
    }
}
