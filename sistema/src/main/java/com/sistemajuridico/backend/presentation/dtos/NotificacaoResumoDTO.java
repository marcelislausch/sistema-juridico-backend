package com.sistemajuridico.backend.presentation.dtos;

import java.util.ArrayList;
import java.util.List;

public record NotificacaoResumoDTO(
        List<NotificacaoItemDTO> notificacoes,
        int quantidadeAgenda,
        int quantidadeFinanceiro
) {
    public NotificacaoResumoDTO {
        if (notificacoes == null) {
            notificacoes = new ArrayList<>();
        }
    }
}
