package com.juridia.sistema.core.usecases;

import com.juridia.sistema.core.domain.Audiencia;
import com.juridia.sistema.infrastructure.persistence.AudienciaRepository;
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
