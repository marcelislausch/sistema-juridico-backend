package com.juridia.sistema.core.usecases;

import com.juridia.sistema.core.domain.Audiencia;
import com.juridia.sistema.core.domain.Processo;
import com.juridia.sistema.infrastructure.persistence.AudienciaRepository;
import com.juridia.sistema.infrastructure.persistence.ProcessoRepository;
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
            throw new RuntimeException("Não é possível agendar uma nova audiência com data retroativa.");
        }

        Optional<Processo> processoBusca = processoRepository.findById(processoId);
        if (!processoBusca.isPresent()) {
            throw new RuntimeException("Processo não encontrado no sistema!");
        }
        Processo processo = processoBusca.get();
        audiencia.setProcesso(processo);
        return audienciaRepository.save(audiencia);
    }
}
