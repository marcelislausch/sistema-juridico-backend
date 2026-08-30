package com.sistemajuridico.backend.presentation.dtos;

import java.math.BigDecimal;
import java.util.List;

public record ResumoDashboardDTO(
        Integer totalClientesAtivos,
        Integer totalProcessosAndamento,
        Integer tarefasPendentesHoje,
        List<TarefaDTO> proximasTarefas,
        BigDecimal totalReceberHoje,
        List<FaturamentoDTO> proximasFaturasReceber
) {
}
