package com.sistemajuridico.backend.presentation.dtos.pje;

import java.time.LocalDateTime;

public record SincronizacaoPjeResultadoDTO(
        int totalEncontradas,
        int novasIntimacoes,
        int andamentosCriados,
        String numeroOabConsultada,
        String ufOabConsultada,
        String mensagem,
        LocalDateTime executadoEm
) {}
