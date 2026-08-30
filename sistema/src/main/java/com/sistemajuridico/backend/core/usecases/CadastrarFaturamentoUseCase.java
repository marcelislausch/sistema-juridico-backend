package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Faturamento;
import com.sistemajuridico.backend.core.domain.Processo;
import com.sistemajuridico.backend.core.domain.exceptions.RecursoNaoEncontradoException;
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
            Optional<Processo> optProcesso = processoRepository.findById(processoId);
            if (optProcesso.isEmpty()) {
                throw new RecursoNaoEncontradoException("Processo não encontrado no sistema!");
            }
            Processo processo = optProcesso.get();
            faturamento.setProcesso(processo);
        }
        return faturamentoRepository.save(faturamento);
    }
}
