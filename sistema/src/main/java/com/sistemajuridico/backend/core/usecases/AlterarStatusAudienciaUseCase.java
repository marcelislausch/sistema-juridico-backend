package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Audiencia;
import com.sistemajuridico.backend.core.domain.enums.StatusAudienciaEnum;
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
        Optional<Audiencia> busca = audienciaRepository.findById(audienciaId);
        if (!busca.isPresent()) {
            throw new RuntimeException("Audiência não encontrada no sistema!");
        }

        Audiencia audiencia = busca.get();
        audiencia.setStatus(novoStatus);

        return audienciaRepository.save(audiencia);
    }
}

