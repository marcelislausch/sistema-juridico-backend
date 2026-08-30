package com.sistemajuridico.backend.presentation.dtos;

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
        UUID usuarioId,

        UUID processoId
) {

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
        UUID processoId = null;
        if (tarefa.getProcesso() != null) {
            processoId = tarefa.getProcesso().getId();
        }

        UUID usuarioId = null;
        if (tarefa.getUsuario() != null) {
            usuarioId = tarefa.getUsuario().getId();
        }

        return new TarefaDTO(
                tarefa.getId(),
                tarefa.getDescricao(),
                tarefa.getDataVencimento(),
                tarefa.getConcluida(),
                tarefa.getTipo(),
                usuarioId,
                processoId
        );
    }
}
