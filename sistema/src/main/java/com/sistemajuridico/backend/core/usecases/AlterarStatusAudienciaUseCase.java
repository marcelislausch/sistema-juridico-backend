package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Audiencia;
import com.sistemajuridico.backend.core.domain.enums.StatusAudienciaEnum;
import com.sistemajuridico.backend.core.domain.exceptions.RecursoNaoEncontradoException;
import com.sistemajuridico.backend.infrastructure.persistence.AudienciaRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AlterarStatusAudienciaUseCase {

    private final AudienciaRepository audienciaRepository;

    public AlterarStatusAudienciaUseCase(AudienciaRepository audienciaRepository) {
        this.audienciaRepository = audienciaRepository;
    }

    public Audiencia executar(UUID audienciaId, StatusAudienciaEnum novoStatus) {
        Optional<Audiencia> optAudiencia = audienciaRepository.findById(audienciaId);
        if (optAudiencia.isEmpty()) {
            throw new RecursoNaoEncontradoException("Audiência não encontrada no sistema!");
        }

        Audiencia audiencia = optAudiencia.get();
        audiencia.setStatus(novoStatus);
        return audienciaRepository.save(audiencia);
    }
}
