package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Audiencia;
import com.sistemajuridico.backend.core.domain.exceptions.RecursoNaoEncontradoException;
import com.sistemajuridico.backend.infrastructure.persistence.AudienciaRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AnexarResumoIAUseCase {

    private final AudienciaRepository audienciaRepository;

    public AnexarResumoIAUseCase(AudienciaRepository audienciaRepository) {
        this.audienciaRepository = audienciaRepository;
    }

    public Audiencia executar(UUID audienciaId, String resumoGerado) {
        Optional<Audiencia> optAudiencia = this.audienciaRepository.findById(audienciaId);
        if (optAudiencia.isEmpty()) {
            throw new RecursoNaoEncontradoException("Audiência não encontrada no sistema!");
        }

        Audiencia audiencia = optAudiencia.get();
        audiencia.setResumoPreparatorioIa(resumoGerado);

        return this.audienciaRepository.save(audiencia);
    }
}
