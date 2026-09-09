package com.sistemajuridico.backend.presentation.dtos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public record ResumoDashboardDTO(
        Integer totalClientesAtivos,
        Integer totalProcessosAndamento,
        Integer tarefasPendentesHoje,
        List<TarefaDTO> proximasTarefas,
        BigDecimal totalReceberHoje,
        List<FaturamentoDTO> proximasFaturasReceber,
        Integer audienciasHoje,
        List<AudienciaDTO> proximasAudiencias
) {
    public ResumoDashboardDTO {
        if (totalClientesAtivos == null) {
            totalClientesAtivos = 0;
        }
        if (totalProcessosAndamento == null) {
            totalProcessosAndamento = 0;
        }
        if (tarefasPendentesHoje == null) {
            tarefasPendentesHoje = 0;
        }
        if (proximasTarefas == null) {
            proximasTarefas = new ArrayList<>();
        }
        if (totalReceberHoje == null) {
            totalReceberHoje = BigDecimal.ZERO;
        }
        if (proximasFaturasReceber == null) {
            proximasFaturasReceber = new ArrayList<>();
        }
        if (audienciasHoje == null) {
            audienciasHoje = 0;
        }
        if (proximasAudiencias == null) {
            proximasAudiencias = new ArrayList<>();
        }
    }
}

