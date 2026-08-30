package com.sistemajuridico.backend.presentation.controllers;

import com.sistemajuridico.backend.core.domain.Tarefa;
import com.sistemajuridico.backend.core.usecases.ConcluirTarefaUseCase;
import com.sistemajuridico.backend.core.usecases.CriarTarefaUseCase;
import com.sistemajuridico.backend.core.usecases.ListarTarefasDashboardUseCase;
import com.sistemajuridico.backend.presentation.dtos.TarefaDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tarefas")
public class TarefaController {

    private final CriarTarefaUseCase criarTarefaUseCase;
    private final ConcluirTarefaUseCase concluirTarefaUseCase;
    private final ListarTarefasDashboardUseCase listarTarefasDashboardUseCase;

    public TarefaController(CriarTarefaUseCase criarTarefaUseCase,
                            ConcluirTarefaUseCase concluirTarefaUseCase,
                            ListarTarefasDashboardUseCase listarTarefasDashboardUseCase) {
        this.criarTarefaUseCase = criarTarefaUseCase;
        this.concluirTarefaUseCase = concluirTarefaUseCase;
        this.listarTarefasDashboardUseCase = listarTarefasDashboardUseCase;
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
}
