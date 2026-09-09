package com.sistemajuridico.backend.presentation.dtos;

import com.sistemajuridico.backend.core.domain.enums.DestinoNotificacaoEnum;
import com.sistemajuridico.backend.core.domain.enums.TipoNotificacaoEnum;
import com.sistemajuridico.backend.core.domain.enums.TipoRecursoNotificacaoEnum;

import java.util.UUID;

public record NotificacaoItemDTO(
        UUID id,
        TipoNotificacaoEnum tipo,
        String titulo,
        String descricao,
        String horario,
        DestinoNotificacaoEnum destino,
        TipoRecursoNotificacaoEnum recursoTipo,
        UUID recursoId
) {
}
