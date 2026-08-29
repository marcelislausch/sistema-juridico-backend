package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Audiencia;
import com.sistemajuridico.backend.infrastructure.persistence.AudienciaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ListarAudienciasPorProcessoUseCase {

    private final AudienciaRepository audienciaRepository;

    public ListarAudienciasPorProcessoUseCase(AudienciaRepository audienciaRepository) {
        this.audienciaRepository = audienciaRepository;
    }

    public List<Audiencia> executar(UUID processoId) {
        return audienciaRepository.findByProcessoId(processoId);
    }
}

