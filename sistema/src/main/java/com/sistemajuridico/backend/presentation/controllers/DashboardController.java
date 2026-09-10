package com.sistemajuridico.backend.presentation.controllers;

import com.sistemajuridico.backend.presentation.openapi.DashboardControllerOpenApi;

import com.sistemajuridico.backend.core.domain.Usuario;
import com.sistemajuridico.backend.core.domain.exceptions.RecursoNaoEncontradoException;
import com.sistemajuridico.backend.core.domain.exceptions.RegraNegocioException;
import com.sistemajuridico.backend.core.usecases.DashboardAdvogadoUseCase;
import com.sistemajuridico.backend.infrastructure.persistence.UsuarioRepository;
import com.sistemajuridico.backend.presentation.dtos.ResumoDashboardDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController implements DashboardControllerOpenApi {

    private final DashboardAdvogadoUseCase dashboardAdvogadoUseCase;
    private final UsuarioRepository usuarioRepository;

    public DashboardController(DashboardAdvogadoUseCase dashboardAdvogadoUseCase,
                               UsuarioRepository usuarioRepository) {
        this.dashboardAdvogadoUseCase = dashboardAdvogadoUseCase;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @GetMapping
    public ResponseEntity<ResumoDashboardDTO> obterDashboardAutenticado(Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new RegraNegocioException("Usuário não autenticado no sistema!");
        }

        String email = principal.getName();
        Optional<Usuario> optUsuario = this.usuarioRepository.findByEmail(email);
        if (optUsuario.isEmpty()) {
            throw new RecursoNaoEncontradoException("Usuário autenticado não encontrado no sistema!");
        }

        UUID usuarioId = optUsuario.get().getId();
        ResumoDashboardDTO resumo = this.dashboardAdvogadoUseCase.executar(usuarioId);
        return ResponseEntity.ok(resumo);
    }

    @Override
    @GetMapping("/{usuarioId}")
    public ResponseEntity<ResumoDashboardDTO> obterDashboard(@PathVariable UUID usuarioId) {
        ResumoDashboardDTO resumo = this.dashboardAdvogadoUseCase.executar(usuarioId);
        return ResponseEntity.ok(resumo);
    }
}
