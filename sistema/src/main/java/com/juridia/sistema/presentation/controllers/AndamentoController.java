package com.juridia.sistema.presentation.controllers;

import com.juridia.sistema.core.domain.Andamento;
import com.juridia.sistema.core.usecases.CadastrarAndamentoUseCase;
import com.juridia.sistema.presentation.dtos.AndamentoDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/processos")
public class AndamentoController {

    private final CadastrarAndamentoUseCase cadastrarAndamentoUseCase;

    public AndamentoController(CadastrarAndamentoUseCase cadastrarAndamentoUseCase) {
        this.cadastrarAndamentoUseCase = cadastrarAndamentoUseCase;
    }

    @PostMapping("/{processoId}/andamentos")
    public ResponseEntity<AndamentoDTO> criar(@PathVariable UUID processoId, @RequestBody AndamentoDTO dto) {
        Andamento andamento = dto.toEntity();
        Andamento andamentoSalvo = cadastrarAndamentoUseCase.executar(andamento, processoId);
        return ResponseEntity.status(HttpStatus.CREATED).body(AndamentoDTO.fromEntity(andamentoSalvo));
    }
}
