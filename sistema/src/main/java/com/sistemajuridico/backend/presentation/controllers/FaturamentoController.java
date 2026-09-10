package com.sistemajuridico.backend.presentation.controllers;

import com.sistemajuridico.backend.presentation.openapi.FaturamentoControllerOpenApi;

import com.sistemajuridico.backend.core.domain.Faturamento;
import com.sistemajuridico.backend.core.domain.enums.NaturezaFaturamentoEnum;
import com.sistemajuridico.backend.core.domain.enums.StatusFaturamentoEnum;
import com.sistemajuridico.backend.core.domain.enums.TipoFaturamentoEnum;
import com.sistemajuridico.backend.core.usecases.CadastrarFaturamentoUseCase;
import com.sistemajuridico.backend.core.usecases.LiquidarFaturamentoUseCase;
import com.sistemajuridico.backend.core.usecases.ListarFaturamentosUseCase;
import com.sistemajuridico.backend.core.usecases.ObterResumoFinanceiroUseCase;
import com.sistemajuridico.backend.presentation.dtos.FaturamentoDTO;
import com.sistemajuridico.backend.presentation.dtos.LiquidarFaturamentoDTO;
import com.sistemajuridico.backend.presentation.dtos.ResumoFinanceiroDTO;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/faturamentos")
public class FaturamentoController implements FaturamentoControllerOpenApi {

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

    @Override
    @GetMapping("/resumo")
    public ResponseEntity<ResumoFinanceiroDTO> obterResumo() {
        ResumoFinanceiroDTO resumo = this.obterResumoFinanceiroUseCase.executar();
        return ResponseEntity.ok(resumo);
    }

    @Override
    @PostMapping
    public ResponseEntity<FaturamentoDTO> criar(@RequestBody @Valid FaturamentoDTO dto) {
        Faturamento faturamento = dto.toEntity();
        Faturamento faturamentoSalvo = cadastrarFaturamentoUseCase.executar(faturamento, dto.processoId());
        return ResponseEntity.status(HttpStatus.CREATED).body(FaturamentoDTO.fromEntity(faturamentoSalvo));
    }

    @Override
    @PatchMapping("/{id}/pagar")
    public ResponseEntity<FaturamentoDTO> liquidar(@PathVariable UUID id, @RequestBody @Valid LiquidarFaturamentoDTO dto) {
        Faturamento faturamentoLiquidado = liquidarFaturamentoUseCase.executar(id, dto.dataPagamento());
        return ResponseEntity.ok(FaturamentoDTO.fromEntity(faturamentoLiquidado));
    }

    @Override
    @GetMapping("/processo/{processoId}")
    public ResponseEntity<List<FaturamentoDTO>> listarPorProcesso(@PathVariable UUID processoId) {
        List<Faturamento> faturamentos = listarFaturamentosUseCase.buscarPorProcesso(processoId);
        List<FaturamentoDTO> response = new ArrayList<>();
        for (Faturamento faturamento : faturamentos) {
            response.add(FaturamentoDTO.fromEntity(faturamento));
        }
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping
    public ResponseEntity<Page<FaturamentoDTO>> listarTodos(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "termoBusca", required = false) String termoBusca,
            @RequestParam(name = "termo", required = false) String termoParam,
            @RequestParam(name = "status", required = false) StatusFaturamentoEnum status,
            @RequestParam(name = "natureza", required = false) NaturezaFaturamentoEnum natureza,
            @RequestParam(name = "tipo", required = false) TipoFaturamentoEnum tipo,
            @RequestParam(name = "vencimentoDe", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate vencimentoDe,
            @RequestParam(name = "vencimentoAte", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate vencimentoAte,
            @RequestParam(name = "processoId", required = false) UUID processoId,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {
        String termo = null;
        if (q != null && !q.trim().isEmpty()) {
            termo = q.trim();
        } else if (termoBusca != null && !termoBusca.trim().isEmpty()) {
            termo = termoBusca.trim();
        } else if (termoParam != null && !termoParam.trim().isEmpty()) {
            termo = termoParam.trim();
        }

        Page<Faturamento> paginaFaturamentos = this.listarFaturamentosUseCase.buscarComFiltros(
                termo, status, natureza, tipo, vencimentoDe, vencimentoAte, processoId, pageable
        );
        List<FaturamentoDTO> dtos = new ArrayList<>();
        for (Faturamento faturamento : paginaFaturamentos.getContent()) {
            dtos.add(FaturamentoDTO.fromEntity(faturamento));
        }
        Page<FaturamentoDTO> pageDtos = new PageImpl<>(dtos, paginaFaturamentos.getPageable(), paginaFaturamentos.getTotalElements());
        return ResponseEntity.ok(pageDtos);
    }
}
