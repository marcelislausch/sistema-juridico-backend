package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Processo;
import com.sistemajuridico.backend.core.domain.enums.FaseProcessualEnum;
import com.sistemajuridico.backend.infrastructure.persistence.ProcessoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ListarProcessosUseCase {

    private final ProcessoRepository processoRepository;

    public ListarProcessosUseCase(ProcessoRepository processoRepository) {
        this.processoRepository = processoRepository;
    }

    public Page<Processo> executar(String termoBusca, Boolean arquivado, Pageable pageable) {
        boolean temTermo = termoBusca != null && !termoBusca.trim().isEmpty();
        String termo = temTermo ? termoBusca.trim() : null;

        if (temTermo) {
            if (arquivado != null) {
                if (arquivado) {
                    return this.processoRepository.buscarPorFaseETermo(FaseProcessualEnum.ARQUIVADO, termo, pageable);
                } else {
                    return this.processoRepository.buscarPorFaseDiferenteETermo(FaseProcessualEnum.ARQUIVADO, termo, pageable);
                }
            } else {
                return this.processoRepository.buscarPorTermo(termo, pageable);
            }
        } else {
            if (arquivado != null) {
                if (arquivado) {
                    return this.processoRepository.findByFaseAtual(FaseProcessualEnum.ARQUIVADO, pageable);
                } else {
                    return this.processoRepository.findByFaseAtualNot(FaseProcessualEnum.ARQUIVADO, pageable);
                }
            } else {
                return this.processoRepository.findAll(pageable);
            }
        }
    }

    public Page<Processo> executar(Pageable pageable) {
        return executar(null, null, pageable);
    }
}
