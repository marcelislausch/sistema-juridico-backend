package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Faturamento;
import com.sistemajuridico.backend.core.domain.enums.StatusFaturamentoEnum;
import com.sistemajuridico.backend.core.domain.exceptions.RecursoNaoEncontradoException;
import com.sistemajuridico.backend.core.domain.exceptions.RegraNegocioException;
import com.sistemajuridico.backend.infrastructure.persistence.FaturamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
public class LiquidarParcialFaturamentoUseCase {

    private final FaturamentoRepository faturamentoRepository;

    public LiquidarParcialFaturamentoUseCase(FaturamentoRepository faturamentoRepository) {
        this.faturamentoRepository = faturamentoRepository;
    }

    @Transactional
    public Faturamento executar(UUID faturamentoId, BigDecimal valorPago, LocalDate novaDataVencimento, LocalDate dataPagamento) {
        Optional<Faturamento> optFaturamento = this.faturamentoRepository.findById(faturamentoId);
        if (optFaturamento.isEmpty()) {
            throw new RecursoNaoEncontradoException("Faturamento não encontrado!");
        }

        Faturamento original = optFaturamento.get();

        if (original.getStatus() == StatusFaturamentoEnum.PAGO) {
            throw new RegraNegocioException("Não é possível realizar baixa parcial em um faturamento já liquidado.");
        }

        if (original.getStatus() == StatusFaturamentoEnum.CANCELADO) {
            throw new RegraNegocioException("Não é possível realizar baixa parcial em um faturamento cancelado.");
        }

        if (valorPago == null || valorPago.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraNegocioException("O valor pago deve ser maior que zero.");
        }

        BigDecimal valorOriginal = original.getValor();
        if (valorOriginal == null || valorPago.compareTo(valorOriginal) >= 0) {
            throw new RegraNegocioException("Para baixa parcial, o valor pago deve ser estritamente menor que o valor total da fatura.");
        }

        if (novaDataVencimento == null) {
            throw new RegraNegocioException("A nova data de vencimento é obrigatória para o saldo remanescente.");
        }

        LocalDate dataEfetiva = dataPagamento != null ? dataPagamento : LocalDate.now();

        // 1. Atualiza o faturamento original
        original.setValor(valorPago);
        original.setStatus(StatusFaturamentoEnum.PAGO);
        original.setDataPagamento(dataEfetiva);

        // 2. Instancia o faturamento desdobrado com o saldo remanescente
        BigDecimal saldoRemanescente = valorOriginal.subtract(valorPago);

        Faturamento desdobrado = new Faturamento();

        String descricaoOriginal = original.getDescricao();
        if (descricaoOriginal == null) {
            descricaoOriginal = "";
        }
        String sufixo = " (Saldo Remanescente)";
        if (descricaoOriginal.endsWith(sufixo)) {
            desdobrado.setDescricao(descricaoOriginal);
        } else {
            desdobrado.setDescricao(descricaoOriginal + sufixo);
        }
        desdobrado.setValor(saldoRemanescente);
        desdobrado.setTipo(original.getTipo());
        desdobrado.setStatus(StatusFaturamentoEnum.PENDENTE);
        desdobrado.setNatureza(original.getNatureza());
        desdobrado.setDataVencimento(novaDataVencimento);
        desdobrado.setDataPagamento(null);
        desdobrado.setProcesso(original.getProcesso());

        desdobrado.setNumeroParcela(original.getNumeroParcela());
        desdobrado.setTotalParcelas(original.getTotalParcelas());
        desdobrado.setOrigemPagamento(original.getOrigemPagamento());
        desdobrado.setFormaRepasse(original.getFormaRepasse());
        desdobrado.setDadosBancariosCliente(original.getDadosBancariosCliente());

        // 3. Salva ambos
        this.faturamentoRepository.save(desdobrado);
        return this.faturamentoRepository.save(original);
    }
}
