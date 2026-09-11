package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Faturamento;
import com.sistemajuridico.backend.core.domain.Processo;
import com.sistemajuridico.backend.core.domain.exceptions.RegraNegocioException;
import com.sistemajuridico.backend.infrastructure.persistence.FaturamentoRepository;
import com.sistemajuridico.backend.infrastructure.persistence.ProcessoRepository;
import com.sistemajuridico.backend.presentation.dtos.FaturamentoDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class GerarParcelamentoUseCase {

    private final FaturamentoRepository faturamentoRepository;
    private final ProcessoRepository processoRepository;

    public GerarParcelamentoUseCase(FaturamentoRepository faturamentoRepository, ProcessoRepository processoRepository) {
        this.faturamentoRepository = faturamentoRepository;
        this.processoRepository = processoRepository;
    }

    @Transactional
    public List<Faturamento> executar(List<FaturamentoDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            throw new RegraNegocioException("A lista de faturamentos não pode ser vazia para o parcelamento.");
        }

        List<Faturamento> entidades = new ArrayList<>();

        for (int i = 0; i < dtos.size(); i++) {
            FaturamentoDTO dto = dtos.get(i);
            if (dto == null) {
                continue;
            }

            Faturamento faturamento = dto.toEntity();

            UUID processoId = dto.processoId();
            if (processoId != null) {
                Optional<Processo> optProcesso = this.processoRepository.findById(processoId);
                if (optProcesso.isPresent()) {
                    faturamento.setProcesso(optProcesso.get());
                }
            }

            entidades.add(faturamento);
        }

        if (entidades.isEmpty()) {
            throw new RegraNegocioException("Nenhum faturamento válido foi fornecido para cadastro.");
        }

        List<Faturamento> salvos = this.faturamentoRepository.saveAll(entidades);
        List<Faturamento> resultado = new ArrayList<>();
        for (Faturamento faturamento : salvos) {
            resultado.add(faturamento);
        }

        return resultado;
    }
}
