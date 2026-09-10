package com.sistemajuridico.backend.presentation.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRawValue;
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

        @JsonRawValue
        String resumoPreparatorioIa,

        @NotNull(message = "O processo é obrigatório")
        @JsonAlias({"processoId"})
        ProcessoResumoDTO processo,

        UUID responsavelId
) {

    public AudienciaDTO(
            UUID id,
            LocalDateTime dataHora,
            String local,
            String observacoes,
            StatusAudienciaEnum status,
            String resumoPreparatorioIa,
            ProcessoResumoDTO processo
    ) {
        this(id, dataHora, local, observacoes, status, resumoPreparatorioIa, processo, null);
    }

    public AudienciaDTO(
            UUID id,
            LocalDateTime dataHora,
            String local,
            String observacoes,
            StatusAudienciaEnum status,
            String resumoPreparatorioIa,
            UUID processoId
    ) {
        this(id, dataHora, local, observacoes, status, resumoPreparatorioIa,
                processoId != null ? new ProcessoResumoDTO(processoId, null, null) : null, null);
    }

    public AudienciaDTO(
            UUID id,
            LocalDateTime dataHora,
            String local,
            String observacoes,
            StatusAudienciaEnum status,
            String resumoPreparatorioIa,
            UUID processoId,
            UUID responsavelId
    ) {
        this(id, dataHora, local, observacoes, status, resumoPreparatorioIa,
                processoId != null ? new ProcessoResumoDTO(processoId, null, null) : null, responsavelId);
    }

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
        if (audiencia == null) {
            return null;
        }

        ProcessoResumoDTO processo = null;
        UUID responsavelId = null;

        if (audiencia.getProcesso() != null) {
            processo = ProcessoResumoDTO.fromEntity(audiencia.getProcesso());
            if (audiencia.getProcesso().getAdvogado() != null) {
                responsavelId = audiencia.getProcesso().getAdvogado().getId();
            }
        }

        StatusAudienciaEnum status = audiencia.getStatus();
        if (status == null) {
            status = StatusAudienciaEnum.AGENDADA;
        }

        return new AudienciaDTO(
                audiencia.getId(),
                audiencia.getDataHora(),
                audiencia.getLocal(),
                audiencia.getObservacoes(),
                status,
                audiencia.getResumoPreparatorioIa(),
                processo,
                responsavelId
        );
    }

    @JsonIgnore
    public UUID processoId() {
        return this.processo != null ? this.processo.id() : null;
    }
}
