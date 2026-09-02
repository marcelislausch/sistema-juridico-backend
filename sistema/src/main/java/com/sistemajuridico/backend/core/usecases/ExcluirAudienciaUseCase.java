package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Audiencia;
import com.sistemajuridico.backend.core.domain.exceptions.RecursoNaoEncontradoException;
import com.sistemajuridico.backend.infrastructure.persistence.AudienciaRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ExcluirAudienciaUseCase {

    private final AudienciaRepository audienciaRepository;

    public ExcluirAudienciaUseCase(AudienciaRepository audienciaRepository) {
        this.audienciaRepository = audienciaRepository;
    }

    public void executar(UUID id) {
        Optional<Audiencia> optAudiencia = this.audienciaRepository.findById(id);
        if (optAudiencia.isEmpty()) {
            throw new RecursoNaoEncontradoException("Audiência não encontrada no sistema!");
        }

        this.audienciaRepository.delete(optAudiencia.get());
    }
}
