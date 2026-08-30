package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Audiencia;
import com.sistemajuridico.backend.core.domain.exceptions.RecursoNaoEncontradoException;
import com.sistemajuridico.backend.core.domain.exceptions.RegraNegocioException;
import com.sistemajuridico.backend.infrastructure.ai.ResumoAIService;
import com.sistemajuridico.backend.infrastructure.persistence.AudienciaRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class GerarEAnexarResumoAudienciaUseCase {

    private final AudienciaRepository audienciaRepository;
    private final ResumoAIService resumoAIService;

    public GerarEAnexarResumoAudienciaUseCase(AudienciaRepository audienciaRepository,
                                              ResumoAIService resumoAIService) {
        this.audienciaRepository = audienciaRepository;
        this.resumoAIService = resumoAIService;
    }

    public Audiencia executar(UUID audienciaId, String conteudoPeca) {
        if (conteudoPeca == null || conteudoPeca.trim().isEmpty()) {
            throw new RegraNegocioException("O conteúdo da peça processual é obrigatório para a geração do resumo pela IA.");
        }

        Optional<Audiencia> optAudiencia = this.audienciaRepository.findById(audienciaId);
        if (optAudiencia.isEmpty()) {
            throw new RecursoNaoEncontradoException("Audiência não encontrada no sistema!");
        }

        Audiencia audiencia = optAudiencia.get();

        String resumoGerado = this.resumoAIService.resumirPecas(conteudoPeca);
        audiencia.setResumoPreparatorioIa(resumoGerado);

        return this.audienciaRepository.save(audiencia);
    }
}
