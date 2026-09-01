package com.sistemajuridico.backend.presentation.controllers;

import com.sistemajuridico.backend.core.domain.Tarefa;
import com.sistemajuridico.backend.core.domain.Usuario;
import com.sistemajuridico.backend.core.domain.exceptions.RecursoNaoEncontradoException;
import com.sistemajuridico.backend.core.domain.exceptions.RegraNegocioException;
import com.sistemajuridico.backend.core.usecases.ConcluirTarefaUseCase;
import com.sistemajuridico.backend.core.usecases.CriarTarefaUseCase;
import com.sistemajuridico.backend.core.usecases.ListarTarefasDashboardUseCase;
import com.sistemajuridico.backend.core.usecases.ListarTarefasPorPeriodoUseCase;
import com.sistemajuridico.backend.infrastructure.persistence.UsuarioRepository;
import com.sistemajuridico.backend.presentation.dtos.TarefaDTO;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/tarefas")
public class TarefaController {

    private final CriarTarefaUseCase criarTarefaUseCase;
    private final ConcluirTarefaUseCase concluirTarefaUseCase;
    private final ListarTarefasDashboardUseCase listarTarefasDashboardUseCase;
    private final ListarTarefasPorPeriodoUseCase listarTarefasPorPeriodoUseCase;
    private final UsuarioRepository usuarioRepository;

    public TarefaController(CriarTarefaUseCase criarTarefaUseCase,
                            ConcluirTarefaUseCase concluirTarefaUseCase,
                            ListarTarefasDashboardUseCase listarTarefasDashboardUseCase,
                            ListarTarefasPorPeriodoUseCase listarTarefasPorPeriodoUseCase,
                            UsuarioRepository usuarioRepository) {
        this.criarTarefaUseCase = criarTarefaUseCase;
        this.concluirTarefaUseCase = concluirTarefaUseCase;
        this.listarTarefasDashboardUseCase = listarTarefasDashboardUseCase;
        this.listarTarefasPorPeriodoUseCase = listarTarefasPorPeriodoUseCase;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping
    public ResponseEntity<TarefaDTO> criar(@RequestBody @Valid TarefaDTO dto) {
        Tarefa tarefa = dto.toEntity();
        Tarefa tarefaSalva = this.criarTarefaUseCase.executar(tarefa, dto.usuarioId(), dto.processoId());
        return ResponseEntity.status(HttpStatus.CREATED).body(TarefaDTO.fromEntity(tarefaSalva));
    }

    @PatchMapping("/{id}/concluir")
    public ResponseEntity<TarefaDTO> concluir(@PathVariable UUID id) {
        Tarefa tarefaConcluida = this.concluirTarefaUseCase.executar(id);
        return ResponseEntity.ok(TarefaDTO.fromEntity(tarefaConcluida));
    }

    @GetMapping("/dashboard/{usuarioId}")
    public ResponseEntity<List<TarefaDTO>> listarDashboard(@PathVariable UUID usuarioId) {
        List<Tarefa> tarefas = this.listarTarefasDashboardUseCase.executar(usuarioId);
        List<TarefaDTO> response = new ArrayList<>();
        for (Tarefa tarefa : tarefas) {
            response.add(TarefaDTO.fromEntity(tarefa));
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/agenda")
    public ResponseEntity<List<TarefaDTO>> listarAgenda(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new RegraNegocioException("Usuário não autenticado no sistema!");
        }

        String email = principal.getName();
        Optional<Usuario> optUsuario = this.usuarioRepository.findByEmail(email);
        if (optUsuario.isEmpty()) {
            throw new RecursoNaoEncontradoException("Usuário autenticado não encontrado no sistema!");
        }

        UUID usuarioId = optUsuario.get().getId();
        List<TarefaDTO> response = this.listarTarefasPorPeriodoUseCase.executar(usuarioId, inicio, fim);
        return ResponseEntity.ok(response);
    }
}
