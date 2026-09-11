package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Cliente;
import com.sistemajuridico.backend.core.domain.Faturamento;
import com.sistemajuridico.backend.core.domain.enums.NaturezaFaturamentoEnum;
import com.sistemajuridico.backend.core.domain.enums.StatusFaturamentoEnum;
import com.sistemajuridico.backend.core.domain.enums.TipoFaturamentoEnum;
import com.sistemajuridico.backend.core.domain.exceptions.RecursoNaoEncontradoException;
import com.sistemajuridico.backend.core.domain.exceptions.RegraNegocioException;
import com.sistemajuridico.backend.infrastructure.persistence.ClienteRepository;
import com.sistemajuridico.backend.infrastructure.persistence.FaturamentoRepository;
import com.sistemajuridico.backend.presentation.dtos.ConsultaAvulsaDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class RegistrarConsultaAvulsaUseCase {

    private final FaturamentoRepository faturamentoRepository;
    private final ClienteRepository clienteRepository;

    public RegistrarConsultaAvulsaUseCase(FaturamentoRepository faturamentoRepository, ClienteRepository clienteRepository) {
        this.faturamentoRepository = faturamentoRepository;
        this.clienteRepository = clienteRepository;
    }

    @Transactional
    public Faturamento executar(ConsultaAvulsaDTO dto) {
        if (dto == null) {
            throw new RegraNegocioException("Os dados da consulta avulsa são obrigatórios!");
        }
        if (dto.clienteId() == null) {
            throw new RegraNegocioException("O identificador do cliente é obrigatório!");
        }
        if (dto.valor() == null || dto.valor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraNegocioException("O valor da consulta deve ser positivo!");
        }
        if (dto.descricao() == null || dto.descricao().trim().isEmpty()) {
            throw new RegraNegocioException("A descrição da consulta é obrigatória!");
        }

        Optional<Cliente> optCliente = clienteRepository.findById(dto.clienteId());
        if (optCliente.isEmpty()) {
            throw new RecursoNaoEncontradoException("Cliente não encontrado no sistema!");
        }
        Cliente cliente = optCliente.get();

        LocalDate dataEfetiva = dto.dataPagamento() != null ? dto.dataPagamento() : LocalDate.now();

        Faturamento faturamento = new Faturamento();
        faturamento.setCliente(cliente);
        faturamento.setProcesso(null);
        faturamento.setDescricao(dto.descricao().trim());
        faturamento.setValor(dto.valor());
        faturamento.setTipo(TipoFaturamentoEnum.CONSULTA_AVULSA);
        faturamento.setNatureza(NaturezaFaturamentoEnum.A_RECEBER);
        faturamento.setStatus(StatusFaturamentoEnum.PAGO);
        faturamento.setDataVencimento(dataEfetiva);
        faturamento.setDataPagamento(dataEfetiva);
        faturamento.setNumeroParcela(1);
        faturamento.setTotalParcelas(1);

        if (dto.formaPagamento() != null && !dto.formaPagamento().trim().isEmpty()) {
            faturamento.setFormaRepasse(dto.formaPagamento().trim());
        }

        return faturamentoRepository.save(faturamento);
    }
}
