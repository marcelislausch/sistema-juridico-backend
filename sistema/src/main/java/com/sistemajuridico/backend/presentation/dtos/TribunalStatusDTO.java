package com.sistemajuridico.backend.presentation.dtos;

import com.sistemajuridico.backend.core.domain.enums.StatusTribunalEnum;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Status de sincronização e saúde das integrações com os tribunais eletrônicos")
public record TribunalStatusDTO(
        @Schema(description = "Status consolidado da comunicação", example = "OPERACIONAL")
        StatusTribunalEnum status,

        @Schema(description = "Timestamp da última verificação")
        LocalDateTime atualizadoEm,

        @Schema(description = "Mensagem descritiva da situação das integrações", example = "Todos os serviços judiciais operando com sincronização regular.")
        String mensagem,

        @Schema(description = "Lista de tribunais monitorados pelo sistema")
        List<String> tribunaisMonitorados
) {
}
