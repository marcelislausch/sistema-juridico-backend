package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.enums.NaturezaFaturamentoEnum;
import com.sistemajuridico.backend.core.domain.enums.StatusFaturamentoEnum;
import com.sistemajuridico.backend.infrastructure.persistence.FaturamentoRepository;
import com.sistemajuridico.backend.presentation.dtos.ResumoFinanceiroDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class ObterResumoFinanceiroUseCase {

    private final FaturamentoRepository faturamentoRepository;

    public ObterResumoFinanceiroUseCase(FaturamentoRepository faturamentoRepository) {
        this.faturamentoRepository = faturamentoRepository;
    }

    public ResumoFinanceiroDTO executar() {
        LocalDate hoje = LocalDate.now();
        LocalDate inicioMes = hoje.withDayOfMonth(1);
        LocalDate fimMes = hoje.withDayOfMonth(hoje.lengthOfMonth());

        BigDecimal totalReceberPendente = this.faturamentoRepository.somarPorNaturezaEStatus(
                NaturezaFaturamentoEnum.A_RECEBER,
                StatusFaturamentoEnum.PENDENTE
        );
        if (totalReceberPendente == null) {
            totalReceberPendente = BigDecimal.ZERO;
        }

        BigDecimal totalPagarPendente = this.faturamentoRepository.somarPorNaturezaEStatus(
                NaturezaFaturamentoEnum.A_PAGAR,
                StatusFaturamentoEnum.PENDENTE
        );
        if (totalPagarPendente == null) {
            totalPagarPendente = BigDecimal.ZERO;
        }

        BigDecimal totalRecebidoMes = this.faturamentoRepository.somarPorNaturezaEStatusEDataPagamentoBetween(
                NaturezaFaturamentoEnum.A_RECEBER,
                StatusFaturamentoEnum.PAGO,
                inicioMes,
                fimMes
        );
        if (totalRecebidoMes == null) {
            totalRecebidoMes = BigDecimal.ZERO;
        }

        BigDecimal totalPagoMes = this.faturamentoRepository.somarPorNaturezaEStatusEDataPagamentoBetween(
                NaturezaFaturamentoEnum.A_PAGAR,
                StatusFaturamentoEnum.PAGO,
                inicioMes,
                fimMes
        );
        if (totalPagoMes == null) {
            totalPagoMes = BigDecimal.ZERO;
        }

        ResumoFinanceiroDTO resumo = new ResumoFinanceiroDTO();
        resumo.setTotalReceberPendente(totalReceberPendente);
        resumo.setTotalPagarPendente(totalPagarPendente);
        resumo.setTotalRecebidoMes(totalRecebidoMes);
        resumo.setTotalPagoMes(totalPagoMes);

        return resumo;
    }
}
