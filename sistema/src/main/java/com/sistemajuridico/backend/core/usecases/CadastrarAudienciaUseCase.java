package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Audiencia;
import com.sistemajuridico.backend.core.domain.Processo;
import com.sistemajuridico.backend.core.domain.exceptions.RecursoNaoEncontradoException;
import com.sistemajuridico.backend.core.domain.exceptions.RegraNegocioException;
import com.sistemajuridico.backend.infrastructure.persistence.AudienciaRepository;
import com.sistemajuridico.backend.infrastructure.persistence.ProcessoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class CadastrarAudienciaUseCase {

    private final AudienciaRepository audienciaRepository;
    private final ProcessoRepository processoRepository;

    public CadastrarAudienciaUseCase(AudienciaRepository audienciaRepository, ProcessoRepository processoRepository) {
        this.audienciaRepository = audienciaRepository;
        this.processoRepository = processoRepository;
    }

    public Audiencia executar(Audiencia audiencia, UUID processoId) {
        if (audiencia.getDataHora() != null && audiencia.getDataHora().isBefore(LocalDateTime.now())) {
            throw new RegraNegocioException("Não é possível agendar uma nova audiência com data retroativa.");
        }

        Optional<Processo> optProcesso = processoRepository.findById(processoId);
        if (optProcesso.isEmpty()) {
            throw new RecursoNaoEncontradoException("Processo não encontrado no sistema!");
        }

        Processo processo = optProcesso.get();
        audiencia.setProcesso(processo);
        return audienciaRepository.save(audiencia);
    }
}
