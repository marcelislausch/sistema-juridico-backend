package com.sistemajuridico.backend.presentation.controllers;

import com.sistemajuridico.backend.core.domain.Faturamento;
import com.sistemajuridico.backend.core.usecases.CadastrarFaturamentoUseCase;
import com.sistemajuridico.backend.core.usecases.LiquidarFaturamentoUseCase;
import com.sistemajuridico.backend.core.usecases.ListarFaturamentosUseCase;
import com.sistemajuridico.backend.core.usecases.ObterResumoFinanceiroUseCase;
import com.sistemajuridico.backend.presentation.dtos.FaturamentoDTO;
import com.sistemajuridico.backend.presentation.dtos.LiquidarFaturamentoDTO;
import com.sistemajuridico.backend.presentation.dtos.ResumoFinanceiroDTO;
import com.sistemajuridico.backend.core.domain.enums.NaturezaFaturamentoEnum;
import com.sistemajuridico.backend.core.domain.enums.StatusFaturamentoEnum;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
    private final ObterResumoFinanceiroUseCase obterResumoFinanceiroUseCase;

    public FaturamentoController(CadastrarFaturamentoUseCase cadastrarFaturamentoUseCase,
                                 LiquidarFaturamentoUseCase liquidarFaturamentoUseCase,
                                 ListarFaturamentosUseCase listarFaturamentosUseCase,
                                 ObterResumoFinanceiroUseCase obterResumoFinanceiroUseCase) {
        this.cadastrarFaturamentoUseCase = cadastrarFaturamentoUseCase;
        this.liquidarFaturamentoUseCase = liquidarFaturamentoUseCase;
        this.listarFaturamentosUseCase = listarFaturamentosUseCase;
        this.obterResumoFinanceiroUseCase = obterResumoFinanceiroUseCase;
    }

    @GetMapping("/resumo")
    public ResponseEntity<ResumoFinanceiroDTO> obterResumo() {
        ResumoFinanceiroDTO resumo = this.obterResumoFinanceiroUseCase.executar();
        return ResponseEntity.ok(resumo);
    }

    @PostMapping
    public ResponseEntity<FaturamentoDTO> criar(@RequestBody @Valid FaturamentoDTO dto) {
        Faturamento faturamento = dto.toEntity();
        Faturamento faturamentoSalvo = cadastrarFaturamentoUseCase.executar(faturamento, dto.processoId());
        return ResponseEntity.status(HttpStatus.CREATED).body(FaturamentoDTO.fromEntity(faturamentoSalvo));
    }

    @PatchMapping("/{id}/pagar")
    public ResponseEntity<FaturamentoDTO> liquidar(@PathVariable UUID id, @RequestBody @Valid LiquidarFaturamentoDTO dto) {
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
    public ResponseEntity<Page<FaturamentoDTO>> listarTodos(
            @RequestParam(required = false) StatusFaturamentoEnum status,
            @RequestParam(required = false) NaturezaFaturamentoEnum natureza,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<Faturamento> paginaFaturamentos = this.listarFaturamentosUseCase.buscarTodos(status, natureza, pageable);
        List<FaturamentoDTO> dtos = new ArrayList<>();
        for (Faturamento faturamento : paginaFaturamentos.getContent()) {
            dtos.add(FaturamentoDTO.fromEntity(faturamento));
        }
        Page<FaturamentoDTO> pageDtos = new PageImpl<>(dtos, paginaFaturamentos.getPageable(), paginaFaturamentos.getTotalElements());
        return ResponseEntity.ok(pageDtos);
    }
}
