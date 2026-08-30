package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.exceptions.RegraNegocioException;
import com.sistemajuridico.backend.infrastructure.ai.ResumoAIService;
import org.springframework.stereotype.Service;

@Service
public class GerarResumoAudienciaUseCase {

    private final ResumoAIService resumoAIService;

    public GerarResumoAudienciaUseCase(ResumoAIService resumoAIService) {
        this.resumoAIService = resumoAIService;
    }

    public String executar(String conteudoPeca) {
        if (conteudoPeca == null || conteudoPeca.trim().isEmpty()) {
            throw new RegraNegocioException("O conteúdo da peça processual é obrigatório para gerar o resumo.");
        }

        return this.resumoAIService.resumirPecas(conteudoPeca);
    }
}
