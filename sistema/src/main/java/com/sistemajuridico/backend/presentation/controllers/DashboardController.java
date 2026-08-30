package com.sistemajuridico.backend.presentation.controllers;

import com.sistemajuridico.backend.core.usecases.DashboardAdvogadoUseCase;
import com.sistemajuridico.backend.presentation.dtos.ResumoDashboardDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardAdvogadoUseCase dashboardAdvogadoUseCase;

    public DashboardController(DashboardAdvogadoUseCase dashboardAdvogadoUseCase) {
        this.dashboardAdvogadoUseCase = dashboardAdvogadoUseCase;
    }

    @GetMapping("/{usuarioId}")
    public ResponseEntity<ResumoDashboardDTO> obterDashboard(@PathVariable UUID usuarioId) {
        ResumoDashboardDTO resumo = this.dashboardAdvogadoUseCase.executar(usuarioId);
        return ResponseEntity.ok(resumo);
    }
}
