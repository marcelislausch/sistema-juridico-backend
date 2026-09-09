package com.sistemajuridico.backend.presentation.controllers;

import com.sistemajuridico.backend.core.domain.Faturamento;
import com.sistemajuridico.backend.core.domain.enums.NaturezaFaturamentoEnum;
import com.sistemajuridico.backend.core.domain.enums.StatusFaturamentoEnum;
import com.sistemajuridico.backend.core.domain.enums.TipoFaturamentoEnum;
import com.sistemajuridico.backend.core.usecases.CadastrarFaturamentoUseCase;
import com.sistemajuridico.backend.core.usecases.LiquidarFaturamentoUseCase;
import com.sistemajuridico.backend.core.usecases.ListarFaturamentosUseCase;
import com.sistemajuridico.backend.core.usecases.ObterResumoFinanceiroUseCase;
import com.sistemajuridico.backend.presentation.dtos.ErroPadraoDTO;
import com.sistemajuridico.backend.presentation.dtos.ErroValidacaoDTO;
import com.sistemajuridico.backend.presentation.dtos.FaturamentoDTO;
import com.sistemajuridico.backend.presentation.dtos.LiquidarFaturamentoDTO;
import com.sistemajuridico.backend.presentation.dtos.ResumoFinanceiroDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Financeiro", description = "Gestão de faturamentos, honorários, liquidação e relatórios financeiros")
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
    @Operation(summary = "Obter resumo financeiro", description = "Recupera os totais consolidados de faturamento, valores pendentes e recebidos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resumo financeiro retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    public ResponseEntity<ResumoFinanceiroDTO> obterResumo() {
        ResumoFinanceiroDTO resumo = this.obterResumoFinanceiroUseCase.executar();
        return ResponseEntity.ok(resumo);
    }

    @PostMapping
    @Operation(summary = "Cadastrar lançamento financeiro", description = "Cria um novo faturamento ou lançamento financeiro")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Faturamento criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de requisição inválidos ou campos incorretos",
                    content = @Content(schema = @Schema(implementation = ErroValidacaoDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Processo informado não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "409", description = "Conflito de integridade",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "422", description = "Regra de negócio violada",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    public ResponseEntity<FaturamentoDTO> criar(@RequestBody @Valid FaturamentoDTO dto) {
        Faturamento faturamento = dto.toEntity();
        Faturamento faturamentoSalvo = cadastrarFaturamentoUseCase.executar(faturamento, dto.processoId());
        return ResponseEntity.status(HttpStatus.CREATED).body(FaturamentoDTO.fromEntity(faturamentoSalvo));
    }

    @PatchMapping("/{id}/pagar")
    @Operation(summary = "Liquidar faturamento", description = "Registra o pagamento ou quitação de um lançamento financeiro")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Faturamento liquidado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Data de pagamento inválida",
                    content = @Content(schema = @Schema(implementation = ErroValidacaoDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Faturamento não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "422", description = "Regra de negócio violada ao liquidar",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    public ResponseEntity<FaturamentoDTO> liquidar(@PathVariable UUID id, @RequestBody @Valid LiquidarFaturamentoDTO dto) {
        Faturamento faturamentoLiquidado = liquidarFaturamentoUseCase.executar(id, dto.dataPagamento());
        return ResponseEntity.ok(FaturamentoDTO.fromEntity(faturamentoLiquidado));
    }

    @GetMapping("/processo/{processoId}")
    @Operation(summary = "Listar faturamentos por processo", description = "Retorna todos os lançamentos financeiros atrelados a um processo judicial")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de faturamentos retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Processo não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    public ResponseEntity<List<FaturamentoDTO>> listarPorProcesso(@PathVariable UUID processoId) {
        List<Faturamento> faturamentos = listarFaturamentosUseCase.buscarPorProcesso(processoId);
        List<FaturamentoDTO> response = new ArrayList<>();
        for (Faturamento faturamento : faturamentos) {
            response.add(FaturamentoDTO.fromEntity(faturamento));
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Listar faturamentos com filtros e paginação", description = "Consulta paginada de lançamentos financeiros com múltiplos filtros")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Página de faturamentos retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de consulta inválidos",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
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
