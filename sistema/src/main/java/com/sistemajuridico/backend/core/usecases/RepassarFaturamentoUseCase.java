package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Faturamento;
import com.sistemajuridico.backend.core.domain.enums.OrigemPagamentoEnum;
import com.sistemajuridico.backend.core.domain.enums.StatusRepasseEnum;
import com.sistemajuridico.backend.core.domain.exceptions.RecursoNaoEncontradoException;
import com.sistemajuridico.backend.core.domain.exceptions.RegraNegocioException;
import com.sistemajuridico.backend.infrastructure.persistence.FaturamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
public class RepassarFaturamentoUseCase {

    private final FaturamentoRepository faturamentoRepository;

    public RepassarFaturamentoUseCase(FaturamentoRepository faturamentoRepository) {
        this.faturamentoRepository = faturamentoRepository;
    }

    @Transactional
    public Faturamento executar(UUID id, LocalDate dataRepasse, String formaRepasse) {
        Optional<Faturamento> optFaturamento = this.faturamentoRepository.findById(id);
        if (optFaturamento.isEmpty()) {
            throw new RecursoNaoEncontradoException("Faturamento não encontrado!");
        }

        Faturamento faturamento = optFaturamento.get();

        if (faturamento.getOrigemPagamento() != OrigemPagamentoEnum.TERCEIRO_SUCUMBENCIA) {
            throw new RegraNegocioException("Operação de repasse permitida apenas para faturamentos com origem em TERCEIRO_SUCUMBENCIA.");
        }

        faturamento.setStatusRepasse(StatusRepasseEnum.REPASSADO);
        faturamento.setDataRepasse(dataRepasse != null ? dataRepasse : LocalDate.now());
        if (formaRepasse != null && !formaRepasse.trim().isEmpty()) {
            faturamento.setFormaRepasse(formaRepasse.trim());
        }

        return this.faturamentoRepository.save(faturamento);
    }
}
