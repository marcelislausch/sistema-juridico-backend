package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Faturamento;
import com.sistemajuridico.backend.core.domain.enums.StatusFaturamentoEnum;
import com.sistemajuridico.backend.core.domain.exceptions.RecursoNaoEncontradoException;
import com.sistemajuridico.backend.core.domain.exceptions.RegraNegocioException;
import com.sistemajuridico.backend.infrastructure.persistence.FaturamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
public class LiquidarFaturamentoUseCase {

    private final FaturamentoRepository faturamentoRepository;

    public LiquidarFaturamentoUseCase(FaturamentoRepository faturamentoRepository) {
        this.faturamentoRepository = faturamentoRepository;
    }

    @Transactional
    public Faturamento executar(UUID faturamentoId, LocalDate dataPagamento) {
        Optional<Faturamento> optFaturamento = faturamentoRepository.findById(faturamentoId);
        if (optFaturamento.isEmpty()) {
            throw new RecursoNaoEncontradoException("Faturamento não encontrado!");
        }

        Faturamento faturamento = optFaturamento.get();
        LocalDate dataEfetiva = dataPagamento != null ? dataPagamento : LocalDate.now();

        faturamento.setStatus(StatusFaturamentoEnum.PAGO);
        faturamento.setDataPagamento(dataEfetiva);

        return faturamentoRepository.save(faturamento);
    }
}
