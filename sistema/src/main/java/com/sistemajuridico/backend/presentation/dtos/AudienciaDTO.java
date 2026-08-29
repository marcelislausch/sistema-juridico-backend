package com.sistemajuridico.backend.presentation.dtos;

import com.sistemajuridico.backend.core.domain.Audiencia;
import com.sistemajuridico.backend.core.domain.enums.StatusAudienciaEnum;

import java.time.LocalDateTime;
import java.util.UUID;

public record AudienciaDTO(
        UUID id,
        LocalDateTime dataHora,
        String local,
        String observacoes,
        StatusAudienciaEnum status,
        UUID processoId
) {

    public Audiencia toEntity() {
        Audiencia audiencia = new Audiencia();
        audiencia.setId(this.id());
        audiencia.setDataHora(this.dataHora());
        audiencia.setLocal(this.local());
        audiencia.setObservacoes(this.observacoes());
        audiencia.setStatus(this.status());
        return audiencia;
    }

    public static AudienciaDTO fromEntity(Audiencia audiencia) {
        UUID processoId = null;
        if (audiencia.getProcesso() != null) {
            processoId = audiencia.getProcesso().getId();
        }
        return new AudienciaDTO(
                audiencia.getId(),
                audiencia.getDataHora(),
                audiencia.getLocal(),
                audiencia.getObservacoes(),
                audiencia.getStatus(),
                processoId
        );
    }
}

