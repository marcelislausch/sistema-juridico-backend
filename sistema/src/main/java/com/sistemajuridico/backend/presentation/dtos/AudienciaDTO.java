package com.sistemajuridico.backend.presentation.dtos;

import com.sistemajuridico.backend.core.domain.Audiencia;
import com.sistemajuridico.backend.core.domain.enums.StatusAudienciaEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record AudienciaDTO(
        UUID id,

        @NotNull(message = "A data e hora da audiência são obrigatórias")
        LocalDateTime dataHora,

        @NotBlank(message = "O local da audiência é obrigatório")
        String local,

        String observacoes,

        StatusAudienciaEnum status,

        String resumoPreparatorioIa,

        @NotNull(message = "O ID do processo é obrigatório")
        UUID processoId
) {

    public Audiencia toEntity() {
        Audiencia audiencia = new Audiencia();
        audiencia.setId(this.id());
        audiencia.setDataHora(this.dataHora());
        audiencia.setLocal(this.local());
        audiencia.setObservacoes(this.observacoes());
        audiencia.setStatus(this.status() != null ? this.status() : StatusAudienciaEnum.AGENDADA);
        audiencia.setResumoPreparatorioIa(this.resumoPreparatorioIa());
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
                audiencia.getResumoPreparatorioIa(),
                processoId
        );
    }
}
