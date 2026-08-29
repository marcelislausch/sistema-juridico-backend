package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Faturamento;
import com.sistemajuridico.backend.core.domain.enums.StatusFaturamentoEnum;
import com.sistemajuridico.backend.infrastructure.persistence.FaturamentoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
public class LiquidarFaturamentoUseCase {

    private final FaturamentoRepository faturamentoRepository;

    public LiquidarFaturamentoUseCase(FaturamentoRepository faturamentoRepository) {
        this.faturamentoRepository = faturamentoRepository;
    }

    public Faturamento executar(UUID faturamentoId, LocalDate dataPagamento) {
        Optional<Faturamento> busca = faturamentoRepository.findById(faturamentoId);
        if (!busca.isPresent()) {
            throw new RuntimeException("Faturamento não encontrado!");
        }

        if (dataPagamento == null) {
            throw new RuntimeException("A data de pagamento é obrigatória para liquidar a fatura!");
        }

        Faturamento faturamento = busca.get();
        faturamento.setStatus(StatusFaturamentoEnum.PAGO);
        faturamento.setDataPagamento(dataPagamento);

        return faturamentoRepository.save(faturamento);
    }
}

