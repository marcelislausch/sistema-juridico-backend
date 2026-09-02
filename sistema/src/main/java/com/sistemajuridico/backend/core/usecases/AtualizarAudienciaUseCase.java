package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Audiencia;
import com.sistemajuridico.backend.core.domain.exceptions.RecursoNaoEncontradoException;
import com.sistemajuridico.backend.infrastructure.persistence.AudienciaRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AtualizarAudienciaUseCase {

    private final AudienciaRepository audienciaRepository;

    public AtualizarAudienciaUseCase(AudienciaRepository audienciaRepository) {
        this.audienciaRepository = audienciaRepository;
    }

    public Audiencia executar(UUID id, Audiencia dadosAtualizados) {
        Optional<Audiencia> optAudiencia = this.audienciaRepository.findById(id);
        if (optAudiencia.isEmpty()) {
            throw new RecursoNaoEncontradoException("Audiência não encontrada no sistema!");
        }

        Audiencia audienciaExistente = optAudiencia.get();
        audienciaExistente.setDataHora(dadosAtualizados.getDataHora());
        audienciaExistente.setLocal(dadosAtualizados.getLocal());
        audienciaExistente.setObservacoes(dadosAtualizados.getObservacoes());

        return this.audienciaRepository.save(audienciaExistente);
    }
}
