package com.sistemajuridico.backend.presentation.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sistemajuridico.backend.core.domain.Tarefa;
import com.sistemajuridico.backend.core.domain.enums.TipoTarefaEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record TarefaDTO(
        UUID id,

        @NotBlank(message = "A descrição da tarefa é obrigatória")
        String descricao,

        @NotNull(message = "A data de vencimento é obrigatória")
        LocalDate dataVencimento,

        Boolean concluida,

        TipoTarefaEnum tipo,

        @NotNull(message = "O usuário responsável é obrigatório")
        @JsonAlias({"usuarioId"})
        UsuarioResumoDTO usuario,

        @JsonAlias({"processoId"})
        ProcessoResumoDTO processo
) {

    public TarefaDTO(
            UUID id,
            String descricao,
            LocalDate dataVencimento,
            Boolean concluida,
            TipoTarefaEnum tipo,
            UUID usuarioId,
            ProcessoResumoDTO processo
    ) {
        this(
                id,
                descricao,
                dataVencimento,
                concluida,
                tipo,
                usuarioId != null ? new UsuarioResumoDTO(usuarioId) : null,
                processo
        );
    }

    public TarefaDTO(
            UUID id,
            String descricao,
            LocalDate dataVencimento,
            Boolean concluida,
            TipoTarefaEnum tipo,
            UUID usuarioId,
            UUID processoId
    ) {
        this(
                id,
                descricao,
                dataVencimento,
                concluida,
                tipo,
                usuarioId != null ? new UsuarioResumoDTO(usuarioId) : null,
                processoId != null ? new ProcessoResumoDTO(processoId, null, null) : null
        );
    }

    public TarefaDTO(
            UUID id,
            String descricao,
            LocalDate dataVencimento,
            Boolean concluida,
            TipoTarefaEnum tipo,
            UsuarioResumoDTO usuario,
            UUID processoId
    ) {
        this(
                id,
                descricao,
                dataVencimento,
                concluida,
                tipo,
                usuario,
                processoId != null ? new ProcessoResumoDTO(processoId, null, null) : null
        );
    }

    public Tarefa toEntity() {
        Tarefa tarefa = new Tarefa();
        tarefa.setId(this.id());
        tarefa.setDescricao(this.descricao());
        tarefa.setDataVencimento(this.dataVencimento());
        tarefa.setConcluida(this.concluida() != null ? this.concluida() : false);
        tarefa.setTipo(this.tipo());
        return tarefa;
    }

    public static TarefaDTO fromEntity(Tarefa tarefa) {
        if (tarefa == null) {
            return null;
        }

        ProcessoResumoDTO processo = null;
        if (tarefa.getProcesso() != null) {
            processo = ProcessoResumoDTO.fromEntity(tarefa.getProcesso());
        }

        UsuarioResumoDTO usuario = null;
        if (tarefa.getUsuario() != null) {
            usuario = UsuarioResumoDTO.fromEntity(tarefa.getUsuario());
        }

        return new TarefaDTO(
                tarefa.getId(),
                tarefa.getDescricao(),
                tarefa.getDataVencimento(),
                tarefa.getConcluida(),
                tarefa.getTipo(),
                usuario,
                processo
        );
    }

    @JsonIgnore
    public UUID processoId() {
        return this.processo != null ? this.processo.id() : null;
    }

    @JsonIgnore
    public UUID usuarioId() {
        return this.usuario != null ? this.usuario.id() : null;
    }
}
