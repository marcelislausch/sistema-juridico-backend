package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Audiencia;
import com.sistemajuridico.backend.core.domain.enums.StatusAudienciaEnum;
import com.sistemajuridico.backend.core.domain.exceptions.RegraNegocioException;
import com.sistemajuridico.backend.infrastructure.persistence.AudienciaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ListarAgendaGlobalUseCase {

    private final AudienciaRepository audienciaRepository;

    public ListarAgendaGlobalUseCase(AudienciaRepository audienciaRepository) {
        this.audienciaRepository = audienciaRepository;
    }

    public List<Audiencia> executar(LocalDateTime inicio,
                                    LocalDateTime fim,
                                    StatusAudienciaEnum status,
                                    UUID processoId,
                                    UUID responsavelId) {
        if (inicio != null && fim != null && inicio.isAfter(fim)) {
            throw new RegraNegocioException("A data inicial não pode ser posterior à data final.");
        }

        String statusStr = null;
        if (status != null) {
            statusStr = status.name();
        }
        return this.audienciaRepository.buscarAgenda(inicio, fim, statusStr, processoId, responsavelId);
    }

    public List<Audiencia> executar(LocalDateTime inicio, LocalDateTime fim) {
        return executar(inicio, fim, null, null, null);
    }
}
