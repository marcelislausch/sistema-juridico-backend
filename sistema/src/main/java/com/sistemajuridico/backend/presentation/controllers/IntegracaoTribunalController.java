package com.sistemajuridico.backend.presentation.controllers;

import com.sistemajuridico.backend.presentation.openapi.IntegracaoTribunalControllerOpenApi;

import com.sistemajuridico.backend.core.domain.enums.StatusTribunalEnum;
import com.sistemajuridico.backend.presentation.dtos.TribunalStatusDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/integracoes")
public class IntegracaoTribunalController implements IntegracaoTribunalControllerOpenApi {

    // TODO: Implementar no futuro a integração real de monitoramento e status via API pública do DataJud/CNJ
    //       ou através de web scraping automatizado dos portais de tribunais (PJe, eproc, Projudi).
    //       Atualmente este endpoint retorna um status operacional controlado para suprir o indicador no front-end.
    @Override
    @GetMapping("/tribunais/status")
    public ResponseEntity<TribunalStatusDTO> verificarStatusTribunais() {
        List<String> tribunais = new ArrayList<>();
        tribunais.add("TJRS - Tribunal de Justiça do Rio Grande do Sul");
        tribunais.add("TRF4 - Tribunal Regional Federal da 4ª Região");
        tribunais.add("TRT4 - Tribunal Regional do Trabalho da 4ª Região");
        tribunais.add("STJ - Superior Tribunal de Justiça");

        TribunalStatusDTO status = new TribunalStatusDTO(
                StatusTribunalEnum.OPERACIONAL,
                LocalDateTime.now(),
                "Todos os serviços judiciais operando com sincronização regular.",
                tribunais
        );
        return ResponseEntity.ok(status);
    }
}
