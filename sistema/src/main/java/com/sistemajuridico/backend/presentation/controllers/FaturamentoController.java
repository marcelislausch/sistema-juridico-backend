package com.sistemajuridico.backend.presentation.controllers;

import com.sistemajuridico.backend.presentation.openapi.FaturamentoControllerOpenApi;

import com.sistemajuridico.backend.core.domain.Faturamento;
import com.sistemajuridico.backend.core.domain.enums.NaturezaFaturamentoEnum;
import com.sistemajuridico.backend.core.domain.enums.StatusFaturamentoEnum;
import com.sistemajuridico.backend.core.domain.enums.TipoFaturamentoEnum;
import com.sistemajuridico.backend.core.usecases.*;
import com.sistemajuridico.backend.presentation.dtos.*;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/faturamento")
public class FaturamentoController implements FaturamentoControllerOpenApi {

    private final CadastrarFaturamentoUseCase cadastrarFaturamentoUseCase;
    private final EditarFaturamentoUseCase editarFaturamentoUseCase;
    private final GerarParcelamentoUseCase gerarParcelamentoUseCase;
    private final LiquidarFaturamentoUseCase liquidarFaturamentoUseCase;
    private final LiquidarParcialFaturamentoUseCase liquidarParcialFaturamentoUseCase;
    private final RepassarFaturamentoUseCase repassarFaturamentoUseCase;
    private final ListarFaturamentosUseCase listarFaturamentosUseCase;
    private final ObterResumoFinanceiroUseCase obterResumoFinanceiroUseCase;
    private final RegistrarConsultaAvulsaUseCase registrarConsultaAvulsaUseCase;

    @Autowired
    public FaturamentoController(CadastrarFaturamentoUseCase cadastrarFaturamentoUseCase,
                                 EditarFaturamentoUseCase editarFaturamentoUseCase,
                                 GerarParcelamentoUseCase gerarParcelamentoUseCase,
                                 LiquidarFaturamentoUseCase liquidarFaturamentoUseCase,
                                 LiquidarParcialFaturamentoUseCase liquidarParcialFaturamentoUseCase,
                                 RepassarFaturamentoUseCase repassarFaturamentoUseCase,
                                 ListarFaturamentosUseCase listarFaturamentosUseCase,
                                 ObterResumoFinanceiroUseCase obterResumoFinanceiroUseCase,
                                 RegistrarConsultaAvulsaUseCase registrarConsultaAvulsaUseCase) {
        this.cadastrarFaturamentoUseCase = cadastrarFaturamentoUseCase;
        this.editarFaturamentoUseCase = editarFaturamentoUseCase;
        this.gerarParcelamentoUseCase = gerarParcelamentoUseCase;
        this.liquidarFaturamentoUseCase = liquidarFaturamentoUseCase;
        this.liquidarParcialFaturamentoUseCase = liquidarParcialFaturamentoUseCase;
        this.repassarFaturamentoUseCase = repassarFaturamentoUseCase;
        this.listarFaturamentosUseCase = listarFaturamentosUseCase;
        this.obterResumoFinanceiroUseCase = obterResumoFinanceiroUseCase;
        this.registrarConsultaAvulsaUseCase = registrarConsultaAvulsaUseCase;
    }

    public FaturamentoController(CadastrarFaturamentoUseCase cadastrarFaturamentoUseCase,
                                 GerarParcelamentoUseCase gerarParcelamentoUseCase,
                                 LiquidarFaturamentoUseCase liquidarFaturamentoUseCase,
                                 LiquidarParcialFaturamentoUseCase liquidarParcialFaturamentoUseCase,
                                 RepassarFaturamentoUseCase repassarFaturamentoUseCase,
                                 ListarFaturamentosUseCase listarFaturamentosUseCase,
                                 ObterResumoFinanceiroUseCase obterResumoFinanceiroUseCase,
                                 RegistrarConsultaAvulsaUseCase registrarConsultaAvulsaUseCase) {
        this(
                cadastrarFaturamentoUseCase,
                null,
                gerarParcelamentoUseCase,
                liquidarFaturamentoUseCase,
                liquidarParcialFaturamentoUseCase,
                repassarFaturamentoUseCase,
                listarFaturamentosUseCase,
                obterResumoFinanceiroUseCase,
                registrarConsultaAvulsaUseCase
        );
    }

    @Override
    @GetMapping("/resumo")
    public ResponseEntity<ResumoFinanceiroDTO> obterResumo() {
        ResumoFinanceiroDTO resumo = this.obterResumoFinanceiroUseCase.executar();
        return ResponseEntity.ok(resumo);
    }

    @Override
    @PostMapping
    public ResponseEntity<FaturamentoDTO> criar(@RequestBody FaturamentoDTO dto) {
        Faturamento faturamento = dto.toEntity();
        Faturamento faturamentoSalvo = cadastrarFaturamentoUseCase.executar(faturamento, dto.processoId());
        return ResponseEntity.status(HttpStatus.CREATED).body(FaturamentoDTO.fromEntity(faturamentoSalvo));
    }

    @Override
    @RequestMapping(value = "/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<FaturamentoDTO> editar(@PathVariable UUID id, @RequestBody EditarFaturamentoDTO dto) {
        Faturamento faturamentoAtualizado = this.editarFaturamentoUseCase.executar(id, dto);
        return ResponseEntity.ok(FaturamentoDTO.fromEntity(faturamentoAtualizado));
    }

    @Override
    @PostMapping("/parcelamento")
    public ResponseEntity<List<FaturamentoDTO>> gerarParcelamento(@RequestBody List<FaturamentoDTO> dtos) {
        List<Faturamento> faturamentosSalvos = this.gerarParcelamentoUseCase.executar(dtos);
        List<FaturamentoDTO> response = new ArrayList<>();
        for (Faturamento faturamento : faturamentosSalvos) {
            response.add(FaturamentoDTO.fromEntity(faturamento));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    @PatchMapping(value = {"/{id}/liquidar", "/{id}/pagar"})
    public ResponseEntity<FaturamentoDTO> liquidar(@PathVariable UUID id, @RequestBody(required = false) LiquidarFaturamentoDTO dto) {
        LocalDate dataPagamento = dto != null ? dto.dataPagamento() : null;
        Faturamento faturamentoLiquidado = this.liquidarFaturamentoUseCase.executar(id, dataPagamento);
        return ResponseEntity.ok(FaturamentoDTO.fromEntity(faturamentoLiquidado));
    }

    @Override
    @PatchMapping("/{id}/liquidar-parcial")
    public ResponseEntity<FaturamentoDTO> liquidarParcial(@PathVariable UUID id, @RequestBody LiquidarParcialDTO dto) {
        Faturamento faturamentoOriginal = this.liquidarParcialFaturamentoUseCase.executar(
                id,
                dto.valorPago(),
                dto.novaDataVencimento(),
                dto.dataPagamento()
        );
        return ResponseEntity.ok(FaturamentoDTO.fromEntity(faturamentoOriginal));
    }

    @Override
    @PatchMapping("/{id}/repassar")
    public ResponseEntity<FaturamentoDTO> repassar(@PathVariable UUID id, @RequestBody(required = false) RepassarFaturamentoDTO dto) {
        LocalDate dataRepasse = dto != null ? dto.dataRepasse() : null;
        String formaRepasse = dto != null ? dto.formaRepasse() : null;
        Faturamento faturamentoRepassado = this.repassarFaturamentoUseCase.executar(id, dataRepasse, formaRepasse);
        return ResponseEntity.ok(FaturamentoDTO.fromEntity(faturamentoRepassado));
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
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FaturamentoPageResponse> listarTodos(
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
        return ResponseEntity.ok(FaturamentoPageResponse.from(pageDtos));
    }

    @Override
    @PostMapping("/consulta-avulsa")
    public ResponseEntity<FaturamentoDTO> registrarConsultaAvulsa(@RequestBody ConsultaAvulsaDTO dto) {
        Faturamento faturamento = this.registrarConsultaAvulsaUseCase.executar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(FaturamentoDTO.fromEntity(faturamento));
    }
}
