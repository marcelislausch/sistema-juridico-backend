package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Faturamento;
import com.sistemajuridico.backend.core.domain.Processo;
import com.sistemajuridico.backend.infrastructure.persistence.FaturamentoRepository;
import com.sistemajuridico.backend.infrastructure.persistence.ProcessoRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class CadastrarFaturamentoUseCase {

    private final FaturamentoRepository faturamentoRepository;
    private final ProcessoRepository processoRepository;

    public CadastrarFaturamentoUseCase(FaturamentoRepository faturamentoRepository, ProcessoRepository processoRepository) {
        this.faturamentoRepository = faturamentoRepository;
        this.processoRepository = processoRepository;
    }

    public Faturamento executar(Faturamento faturamento, UUID processoId) {
        if (processoId != null) {
            Optional<Processo> busca = processoRepository.findById(processoId);
            if (!busca.isPresent()) {
                throw new RuntimeException("Processo não encontrado no sistema!");
            }
            faturamento.setProcesso(busca.get());
        }
        return faturamentoRepository.save(faturamento);
    }
}

