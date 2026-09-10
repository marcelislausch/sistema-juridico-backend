package com.sistemajuridico.backend.infrastructure.ai;

import com.sistemajuridico.backend.core.domain.dto.ResumoAudienciaEstruturadoDTO;

public interface ResumoAIService {
    ResumoAudienciaEstruturadoDTO resumirPecas(String conteudo);
    String resumirChunk(String chunk, int parte, int totalPartes);
}

