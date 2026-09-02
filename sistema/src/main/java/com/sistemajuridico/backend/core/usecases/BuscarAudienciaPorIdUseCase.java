package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Audiencia;
import com.sistemajuridico.backend.core.domain.exceptions.RecursoNaoEncontradoException;
import com.sistemajuridico.backend.infrastructure.persistence.AudienciaRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class BuscarAudienciaPorIdUseCase {

    private final AudienciaRepository audienciaRepository;

    public BuscarAudienciaPorIdUseCase(AudienciaRepository audienciaRepository) {
        this.audienciaRepository = audienciaRepository;
    }

    public Audiencia executar(UUID id) {
        Optional<Audiencia> optAudiencia = this.audienciaRepository.findById(id);
        if (optAudiencia.isEmpty()) {
            throw new RecursoNaoEncontradoException("Audiência não encontrada no sistema!");
        }
        return optAudiencia.get();
    }
}
