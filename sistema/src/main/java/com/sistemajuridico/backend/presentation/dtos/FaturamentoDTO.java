package com.sistemajuridico.backend.presentation.dtos;

import com.sistemajuridico.backend.core.domain.Faturamento;
import com.sistemajuridico.backend.core.domain.enums.NaturezaFaturamentoEnum;
import com.sistemajuridico.backend.core.domain.enums.StatusFaturamentoEnum;
import com.sistemajuridico.backend.core.domain.enums.TipoFaturamentoEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record FaturamentoDTO(
        UUID id,
        String descricao,
        BigDecimal valor,
        TipoFaturamentoEnum tipo,
        StatusFaturamentoEnum status,
        NaturezaFaturamentoEnum natureza,
        LocalDate dataVencimento,
        LocalDate dataPagamento,
        UUID processoId
) {

    public Faturamento toEntity() {
        Faturamento faturamento = new Faturamento();
        faturamento.setId(this.id());
        faturamento.setDescricao(this.descricao());
        faturamento.setValor(this.valor());
        faturamento.setTipo(this.tipo());
        faturamento.setStatus(this.status());
        faturamento.setNatureza(this.natureza());
        faturamento.setDataVencimento(this.dataVencimento());
        faturamento.setDataPagamento(this.dataPagamento());
        return faturamento;
    }

    public static FaturamentoDTO fromEntity(Faturamento faturamento) {
        UUID processoId = null;
        if (faturamento.getProcesso() != null) {
            processoId = faturamento.getProcesso().getId();
        }
        return new FaturamentoDTO(
                faturamento.getId(),
                faturamento.getDescricao(),
                faturamento.getValor(),
                faturamento.getTipo(),
                faturamento.getStatus(),
                faturamento.getNatureza(),
                faturamento.getDataVencimento(),
                faturamento.getDataPagamento(),
                processoId
        );
    }
}

