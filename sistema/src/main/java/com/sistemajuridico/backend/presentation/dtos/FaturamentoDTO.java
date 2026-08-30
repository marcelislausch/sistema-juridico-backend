package com.sistemajuridico.backend.presentation.dtos;

import com.sistemajuridico.backend.core.domain.Faturamento;
import com.sistemajuridico.backend.core.domain.enums.NaturezaFaturamentoEnum;
import com.sistemajuridico.backend.core.domain.enums.StatusFaturamentoEnum;
import com.sistemajuridico.backend.core.domain.enums.TipoFaturamentoEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record FaturamentoDTO(
        UUID id,

        @NotBlank(message = "A descrição do faturamento é obrigatória")
        String descricao,

        @NotNull(message = "O valor é obrigatório")
        @Positive(message = "O valor deve ser positivo")
        BigDecimal valor,

        @NotNull(message = "O tipo de faturamento é obrigatório (HONORARIOS, CUSTAS, DESPESAS_ESCRITORIO)")
        TipoFaturamentoEnum tipo,

        StatusFaturamentoEnum status,

        @NotNull(message = "A natureza do faturamento é obrigatória (A_RECEBER, A_PAGAR)")
        NaturezaFaturamentoEnum natureza,

        @NotNull(message = "A data de vencimento é obrigatória")
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
        faturamento.setStatus(this.status() != null ? this.status() : StatusFaturamentoEnum.PENDENTE);
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
