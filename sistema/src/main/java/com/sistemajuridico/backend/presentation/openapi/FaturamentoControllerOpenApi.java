package com.sistemajuridico.backend.presentation.openapi;

import com.sistemajuridico.backend.core.domain.enums.NaturezaFaturamentoEnum;
import com.sistemajuridico.backend.core.domain.enums.StatusFaturamentoEnum;
import com.sistemajuridico.backend.core.domain.enums.TipoFaturamentoEnum;
import com.sistemajuridico.backend.presentation.dtos.ConsultaAvulsaDTO;
import com.sistemajuridico.backend.presentation.dtos.ErroPadraoDTO;
import com.sistemajuridico.backend.presentation.dtos.ErroValidacaoDTO;
import com.sistemajuridico.backend.presentation.dtos.FaturamentoDTO;
import com.sistemajuridico.backend.presentation.dtos.LiquidarFaturamentoDTO;
import com.sistemajuridico.backend.presentation.dtos.LiquidarParcialDTO;
import com.sistemajuridico.backend.presentation.dtos.RepassarFaturamentoDTO;
import com.sistemajuridico.backend.presentation.dtos.ResumoFinanceiroDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Tag(name = "Financeiro", description = "Gestão de faturamentos, honorários, liquidação e relatórios financeiros")
public interface FaturamentoControllerOpenApi {

    @Operation(summary = "Obter resumo financeiro", description = "Recupera os totais consolidados de faturamento, valores pendentes e recebidos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resumo financeiro retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<ResumoFinanceiroDTO> obterResumo();

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
    ResponseEntity<FaturamentoDTO> criar(@Valid FaturamentoDTO dto);

    @Operation(summary = "Gerar parcelamento (Assistente de Parcelamento)", description = "Cria múltiplos lançamentos financeiros simultaneamente a partir da simulação de parcelas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Parcelas criadas com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de requisição inválidos ou lista vazia",
                    content = @Content(schema = @Schema(implementation = ErroValidacaoDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "422", description = "Regra de negócio violada",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<List<FaturamentoDTO>> gerarParcelamento(@Valid List<FaturamentoDTO> dtos);

    @Operation(summary = "Liquidar faturamento (Baixa Integral)", description = "Registra o pagamento ou quitação integral de um lançamento financeiro")
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
    ResponseEntity<FaturamentoDTO> liquidar(UUID id, @Valid LiquidarFaturamentoDTO dto);

    @Operation(summary = "Baixa parcial de faturamento", description = "Registra o recebimento parcial de um lançamento, quitando o valor pago e desdobrando o saldo restante com nova data de vencimento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Baixa parcial realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de baixa parcial inválidos",
                    content = @Content(schema = @Schema(implementation = ErroValidacaoDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Faturamento não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "422", description = "Regra de negócio violada (ex: valor pago inválido)",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<FaturamentoDTO> liquidarParcial(UUID id, @Valid LiquidarParcialDTO dto);

    @Operation(summary = "Efetivação de repasse ao cliente", description = "Registra a liquidação da transferência de valores de sucumbência ou terceiros ao cliente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Repasse efetivado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Faturamento não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "422", description = "Regra de negócio violada (ex: faturamento sem origem de sucumbência)",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<FaturamentoDTO> repassar(UUID id, @Valid RepassarFaturamentoDTO dto);

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

    @Operation(summary = "Lançar consulta avulsa", description = "Cria e liquida imediatamente um faturamento de consulta jurídica avulsa sem vínculo processual, associado apenas ao cliente com status PAGO")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Consulta avulsa registrada e liquidada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados da consulta inválidos",
                    content = @Content(schema = @Schema(implementation = ErroValidacaoDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "422", description = "Regra de negócio violada",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<FaturamentoDTO> registrarConsultaAvulsa(@Valid ConsultaAvulsaDTO dto);
}
