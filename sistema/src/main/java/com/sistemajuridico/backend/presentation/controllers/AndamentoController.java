package com.sistemajuridico.backend.presentation.controllers;

import com.sistemajuridico.backend.core.domain.Andamento;
import com.sistemajuridico.backend.core.usecases.CadastrarAndamentoUseCase;
import com.sistemajuridico.backend.core.usecases.ListarAndamentosPorProcessoUseCase;
import com.sistemajuridico.backend.presentation.dtos.AndamentoDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/processos")
public class AndamentoController {

    private final CadastrarAndamentoUseCase cadastrarAndamentoUseCase;
    private final ListarAndamentosPorProcessoUseCase listarAndamentosPorProcessoUseCase;

    public AndamentoController(CadastrarAndamentoUseCase cadastrarAndamentoUseCase,
                               ListarAndamentosPorProcessoUseCase listarAndamentosPorProcessoUseCase) {
        this.cadastrarAndamentoUseCase = cadastrarAndamentoUseCase;
        this.listarAndamentosPorProcessoUseCase = listarAndamentosPorProcessoUseCase;
    }

    @PostMapping("/{processoId}/andamentos")
    public ResponseEntity<AndamentoDTO> criar(@PathVariable UUID processoId, @RequestBody AndamentoDTO dto) {
        Andamento andamento = dto.toEntity();
        Andamento andamentoSalvo = cadastrarAndamentoUseCase.executar(andamento, processoId);
        return ResponseEntity.status(HttpStatus.CREATED).body(AndamentoDTO.fromEntity(andamentoSalvo));
    }

    @GetMapping("/{processoId}/andamentos")
    public ResponseEntity<List<AndamentoDTO>> listarPorProcesso(@PathVariable UUID processoId) {
        List<Andamento> andamentos = listarAndamentosPorProcessoUseCase.executar(processoId);
        List<AndamentoDTO> response = new ArrayList<>();
        for (Andamento andamento : andamentos) {
            response.add(AndamentoDTO.fromEntity(andamento));
        }

        return ResponseEntity.ok(response);
    }
}

