package com.juridia.sistema.presentation.controllers;

import com.juridia.sistema.core.domain.Faturamento;
import com.juridia.sistema.core.usecases.CadastrarFaturamentoUseCase;
import com.juridia.sistema.core.usecases.LiquidarFaturamentoUseCase;
import com.juridia.sistema.core.usecases.ListarFaturamentosUseCase;
import com.juridia.sistema.presentation.dtos.FaturamentoDTO;
import com.juridia.sistema.presentation.dtos.LiquidarFaturamentoDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/faturamentos")
public class FaturamentoController {

    private final CadastrarFaturamentoUseCase cadastrarFaturamentoUseCase;
    private final LiquidarFaturamentoUseCase liquidarFaturamentoUseCase;
    private final ListarFaturamentosUseCase listarFaturamentosUseCase;

    public FaturamentoController(CadastrarFaturamentoUseCase cadastrarFaturamentoUseCase,
                                 LiquidarFaturamentoUseCase liquidarFaturamentoUseCase,
                                 ListarFaturamentosUseCase listarFaturamentosUseCase) {
        this.cadastrarFaturamentoUseCase = cadastrarFaturamentoUseCase;
        this.liquidarFaturamentoUseCase = liquidarFaturamentoUseCase;
        this.listarFaturamentosUseCase = listarFaturamentosUseCase;
    }

    @PostMapping
    public ResponseEntity<FaturamentoDTO> criar(@RequestBody FaturamentoDTO dto) {
        Faturamento faturamento = dto.toEntity();
        Faturamento faturamentoSalvo = cadastrarFaturamentoUseCase.executar(faturamento, dto.processoId());
        return ResponseEntity.status(HttpStatus.CREATED).body(FaturamentoDTO.fromEntity(faturamentoSalvo));
    }

    @PatchMapping("/{id}/pagar")
    public ResponseEntity<FaturamentoDTO> liquidar(@PathVariable UUID id, @RequestBody LiquidarFaturamentoDTO dto) {
        Faturamento faturamentoLiquidado = liquidarFaturamentoUseCase.executar(id, dto.dataPagamento());
        return ResponseEntity.ok(FaturamentoDTO.fromEntity(faturamentoLiquidado));
    }

    @GetMapping("/processo/{processoId}")
    public ResponseEntity<List<FaturamentoDTO>> listarPorProcesso(@PathVariable UUID processoId) {
        List<Faturamento> faturamentos = listarFaturamentosUseCase.buscarPorProcesso(processoId);
        List<FaturamentoDTO> response = new ArrayList<>();
        for (Faturamento faturamento : faturamentos) {
            response.add(FaturamentoDTO.fromEntity(faturamento));
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<FaturamentoDTO>> listarTodos() {
        List<Faturamento> faturamentos = listarFaturamentosUseCase.buscarTodos();
        List<FaturamentoDTO> response = new ArrayList<>();
        for (Faturamento faturamento : faturamentos) {
            response.add(FaturamentoDTO.fromEntity(faturamento));
        }
        return ResponseEntity.ok(response);
    }
}
