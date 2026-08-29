package com.juridia.sistema.core.usecases;

import com.juridia.sistema.core.domain.Faturamento;
import com.juridia.sistema.core.domain.Processo;
import com.juridia.sistema.infrastructure.persistence.FaturamentoRepository;
import com.juridia.sistema.infrastructure.persistence.ProcessoRepository;
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
