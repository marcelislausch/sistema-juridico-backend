package com.sistemajuridico.backend.presentation.openapi;

import com.sistemajuridico.backend.core.domain.enums.NaturezaFaturamentoEnum;
import com.sistemajuridico.backend.core.domain.enums.StatusFaturamentoEnum;
import com.sistemajuridico.backend.core.domain.enums.TipoFaturamentoEnum;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Tag(name = "Financeiro", description = "GestÃ£o de faturamentos, honorÃ¡rios, liquidaÃ§Ã£o e relatÃ³rios financeiros")
public interface FaturamentoControllerOpenApi {

    @Operation(summary = "Obter resumo financeiro", description = "Recupera os totais consolidados de faturamento, valores pendentes e recebidos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resumo financeiro retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "NÃ£o autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<ResumoFinanceiroDTO> obterResumo();

    @Operation(summary = "Cadastrar lanÃ§amento financeiro", description = "Cria um novo faturamento ou lanÃ§amento financeiro")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Faturamento criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de requisiÃ§Ã£o invÃ¡lidos ou campos incorretos",
                    content = @Content(schema = @Schema(implementation = ErroValidacaoDTO.class))),
            @ApiResponse(responseCode = "401", description = "NÃ£o autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Processo informado nÃ£o encontrado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "409", description = "Conflito de integridade",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "422", description = "Regra de negÃ³cio violada",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<FaturamentoDTO> criar(FaturamentoDTO dto);

    @Operation(summary = "Liquidar faturamento", description = "Registra o pagamento ou quitaÃ§Ã£o de um lanÃ§amento financeiro")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Faturamento liquidado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Data de pagamento invÃ¡lida",
                    content = @Content(schema = @Schema(implementation = ErroValidacaoDTO.class))),
            @ApiResponse(responseCode = "401", description = "NÃ£o autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Faturamento nÃ£o encontrado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "422", description = "Regra de negÃ³cio violada ao liquidar",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<FaturamentoDTO> liquidar(UUID id, LiquidarFaturamentoDTO dto);

    @Operation(summary = "Listar faturamentos por processo", description = "Retorna todos os lanÃ§amentos financeiros atrelados a um processo judicial")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de faturamentos retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "NÃ£o autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Processo nÃ£o encontrado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<List<FaturamentoDTO>> listarPorProcesso(UUID processoId);

    @Operation(summary = "Listar faturamentos com filtros e paginaÃ§Ã£o", description = "Consulta paginada de lanÃ§amentos financeiros com mÃºltiplos filtros")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PÃ¡gina de faturamentos retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "ParÃ¢metros de consulta invÃ¡lidos",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "401", description = "NÃ£o autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<Page<FaturamentoDTO>> listarTodos(
            String q,
            String termoBusca,
            String termoParam,
            StatusFaturamentoEnum status,
            NaturezaFaturamentoEnum natureza,
            TipoFaturamentoEnum tipo,
            LocalDate vencimentoDe,
            LocalDate vencimentoAte,
            UUID processoId,
            Pageable pageable
    );
}
