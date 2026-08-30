package com.sistemajuridico.backend.presentation.dtos;

import java.util.List;

public record ResumoDashboardDTO(
        Integer totalClientesAtivos,
        Integer totalProcessosAndamento,
        Integer tarefasPendentesHoje,
        List<TarefaDTO> proximasTarefas
) {
}
