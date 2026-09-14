package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Faturamento;
import com.sistemajuridico.backend.core.domain.Processo;
import com.sistemajuridico.backend.core.domain.enums.StatusFaturamentoEnum;
import com.sistemajuridico.backend.core.domain.exceptions.RecursoNaoEncontradoException;
import com.sistemajuridico.backend.core.domain.exceptions.RegraNegocioException;
import com.sistemajuridico.backend.infrastructure.persistence.FaturamentoRepository;
import com.sistemajuridico.backend.infrastructure.persistence.ProcessoRepository;
import com.sistemajuridico.backend.presentation.dtos.EditarFaturamentoDTO;
import com.sistemajuridico.backend.presentation.dtos.FaturamentoDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
public class EditarFaturamentoUseCase {

    private final FaturamentoRepository faturamentoRepository;
    private final ProcessoRepository processoRepository;

    public EditarFaturamentoUseCase(FaturamentoRepository faturamentoRepository, ProcessoRepository processoRepository) {
        this.faturamentoRepository = faturamentoRepository;
        this.processoRepository = processoRepository;
    }

    @Transactional
    public Faturamento executar(UUID id, EditarFaturamentoDTO dto) {
        if (id == null) {
            throw new RegraNegocioException("O identificador do lançamento financeiro é obrigatório!");
        }

        if (dto == null) {
            throw new RegraNegocioException("Os dados para edição do lançamento são obrigatórios!");
        }

        Optional<Faturamento> optFaturamento = this.faturamentoRepository.findById(id);
        if (optFaturamento.isEmpty()) {
            throw new RecursoNaoEncontradoException("Lançamento financeiro não encontrado no sistema!");
        }

        Faturamento faturamento = optFaturamento.get();

        if (dto.valor() != null) {
            if (dto.valor().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RegraNegocioException("O valor do lançamento financeiro deve ser positivo!");
            }
            faturamento.setValor(dto.valor());
        }

        if (dto.descricao() != null && !dto.descricao().trim().isEmpty()) {
            faturamento.setDescricao(dto.descricao().trim());
        }

        if (dto.dataVencimento() != null) {
            faturamento.setDataVencimento(dto.dataVencimento());
        }

        if (dto.categoria() != null) {
            faturamento.setTipo(dto.categoria());
        }

        if (dto.status() != null) {
            faturamento.setStatus(dto.status());
            if (dto.status() == StatusFaturamentoEnum.PAGO && faturamento.getDataPagamento() == null) {
                if (dto.dataPagamento() != null) {
                    faturamento.setDataPagamento(dto.dataPagamento());
                } else {
                    faturamento.setDataPagamento(LocalDate.now());
                }
            }
        }

        if (dto.dataPagamento() != null) {
            faturamento.setDataPagamento(dto.dataPagamento());
        }

        if (dto.natureza() != null) {
            faturamento.setNatureza(dto.natureza());
        }

        if (dto.processoId() != null) {
            Optional<Processo> optProcesso = this.processoRepository.findById(dto.processoId());
            if (optProcesso.isEmpty()) {
                throw new RecursoNaoEncontradoException("Processo informado não encontrado no sistema!");
            }
            faturamento.setProcesso(optProcesso.get());
        }

        return this.faturamentoRepository.save(faturamento);
    }

    @Transactional
    public Faturamento executar(UUID id, FaturamentoDTO dto) {
        if (dto == null) {
            return executar(id, (EditarFaturamentoDTO) null);
        }
        EditarFaturamentoDTO editarDto = new EditarFaturamentoDTO(
                dto.descricao(),
                dto.valor(),
                dto.tipo(),
                dto.status(),
                dto.natureza(),
                dto.dataVencimento(),
                dto.dataPagamento(),
                dto.processoId()
        );
        return executar(id, editarDto);
    }
}
