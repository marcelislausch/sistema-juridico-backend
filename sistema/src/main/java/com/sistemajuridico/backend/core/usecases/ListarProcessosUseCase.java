package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Processo;
import com.sistemajuridico.backend.core.domain.enums.FaseProcessualEnum;
import com.sistemajuridico.backend.infrastructure.persistence.ProcessoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ListarProcessosUseCase {

    private final ProcessoRepository processoRepository;

    public ListarProcessosUseCase(ProcessoRepository processoRepository) {
        this.processoRepository = processoRepository;
    }

    public Page<Processo> executar(String termoBusca,
                                   FaseProcessualEnum fase,
                                   Boolean arquivado,
                                   UUID clienteId,
                                   UUID advogadoId,
                                   Pageable pageable) {
        String termo = (termoBusca != null && !termoBusca.trim().isEmpty()) ? termoBusca.trim() : null;
        String faseStr = null;
        if (fase != null) {
            faseStr = fase.name();
        }
        return this.processoRepository.buscarComFiltros(termo, faseStr, arquivado, clienteId, advogadoId, pageable);
    }

    public Page<Processo> executar(String termoBusca, Boolean arquivado, Pageable pageable) {
        return executar(termoBusca, null, arquivado, null, null, pageable);
    }

    public Page<Processo> executar(Pageable pageable) {
        return executar(null, null, null, null, null, pageable);
    }
}
