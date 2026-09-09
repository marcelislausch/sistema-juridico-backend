package com.sistemajuridico.backend.presentation.controllers;

import com.sistemajuridico.backend.core.domain.exceptions.RegraNegocioException;
import com.sistemajuridico.backend.core.usecases.ObterResumoNotificacoesUseCase;
import com.sistemajuridico.backend.presentation.dtos.NotificacaoResumoDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/notificacoes")
@Tag(name = "Notificações", description = "Central de notificações operacionais unificadas do escritório")
public class NotificacaoController {

    private final ObterResumoNotificacoesUseCase obterResumoNotificacoesUseCase;

    public NotificacaoController(ObterResumoNotificacoesUseCase obterResumoNotificacoesUseCase) {
        this.obterResumoNotificacoesUseCase = obterResumoNotificacoesUseCase;
    }

    @GetMapping("/resumo")
    @Operation(summary = "Obtém as notificações operacionais ativas para a data informada a partir do token de sessão")
    public ResponseEntity<NotificacaoResumoDTO> obterResumo(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new RegraNegocioException("Usuário não autenticado no sistema!");
        }

        String email = principal.getName();
        NotificacaoResumoDTO resumo = this.obterResumoNotificacoesUseCase.executar(email, data);
        return ResponseEntity.ok(resumo);
    }
}
