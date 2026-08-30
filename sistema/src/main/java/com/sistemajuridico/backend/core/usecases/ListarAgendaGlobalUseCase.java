package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Audiencia;
import com.sistemajuridico.backend.core.domain.exceptions.RegraNegocioException;
import com.sistemajuridico.backend.infrastructure.persistence.AudienciaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ListarAgendaGlobalUseCase {

    private final AudienciaRepository audienciaRepository;

    public ListarAgendaGlobalUseCase(AudienciaRepository audienciaRepository) {
        this.audienciaRepository = audienciaRepository;
    }

    public List<Audiencia> executar(LocalDateTime inicio, LocalDateTime fim) {
        if (inicio == null || fim == null) {
            throw new RegraNegocioException("As datas de início e fim são obrigatórias para a consulta da pauta de audiências.");
        }

        if (inicio.isAfter(fim)) {
            throw new RegraNegocioException("A data inicial não pode ser posterior à data final.");
        }

        return this.audienciaRepository.findByDataHoraBetween(inicio, fim);
    }
}
