package com.sistemajuridico.backend.presentation.dtos;

import com.sistemajuridico.backend.core.domain.enums.TipoItemBuscaEnum;

import java.util.UUID;

public record ItemBuscaDTO(
        UUID id,
        TipoItemBuscaEnum tipo,
        String titulo,     // ex: "0001234-56.2026.8.21.0016" ou "Carlos Eduardo"
        String subtitulo,  // ex: "Ação Trabalhista - Fase Inicial" ou "CPF: 123.456.789-00"
        String rota        // "/processos/...", "/clientes/..."
) {
}
