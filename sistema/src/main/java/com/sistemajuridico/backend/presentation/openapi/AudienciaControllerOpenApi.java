package com.sistemajuridico.backend.presentation.openapi;

import com.sistemajuridico.backend.core.domain.enums.StatusAudienciaEnum;
import com.sistemajuridico.backend.presentation.dtos.AudienciaDTO;
import com.sistemajuridico.backend.presentation.dtos.ErroPadraoDTO;
import com.sistemajuridico.backend.presentation.dtos.ErroValidacaoDTO;
import com.sistemajuridico.backend.presentation.dtos.GerarResumoAudienciaDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Tag(name = "AudiÃªncias", description = "GestÃ£o e agendamento de audiÃªncias judiciais e resumos preparatÃ³rios via IA")
public interface AudienciaControllerOpenApi {

    @Operation(summary = "Agendar nova audiÃªncia", description = "Cadastra uma nova audiÃªncia associada a um processo judicial")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "AudiÃªncia agendada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de requisiÃ§Ã£o invÃ¡lidos ou campos obrigatÃ³rios ausentes",
                    content = @Content(schema = @Schema(implementation = ErroValidacaoDTO.class))),
            @ApiResponse(responseCode = "401", description = "NÃ£o autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Processo ou responsÃ¡vel informado nÃ£o encontrado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "409", description = "Conflito de integridade",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "422", description = "Regra de negÃ³cio violada",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<AudienciaDTO> criar(AudienciaDTO dto);

    @Operation(summary = "Listar agenda de audiÃªncias", description = "Retorna audiÃªncias agendadas com filtros por perÃ­odo, status, processo e responsÃ¡vel")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Agenda de audiÃªncias retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "ParÃ¢metros de consulta invÃ¡lidos",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "401", description = "NÃ£o autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<List<AudienciaDTO>> listarAgenda(
            LocalDate inicio,
            LocalDate fim,
            StatusAudienciaEnum status,
            UUID processoId,
            UUID responsavelId
    );

    @Operation(summary = "Listar audiÃªncias por processo", description = "Recupera todas as audiÃªncias vinculadas a um processo especÃ­fico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de audiÃªncias retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "NÃ£o autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Processo nÃ£o encontrado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<List<AudienciaDTO>> listarPorProcesso(UUID processoId);

    @Operation(summary = "Buscar audiÃªncia por ID", description = "Recupera os detalhes de uma audiÃªncia pelo identificador Ãºnico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "AudiÃªncia retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "NÃ£o autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "AudiÃªncia nÃ£o encontrada",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<AudienciaDTO> buscarPorId(UUID id);

    @Operation(summary = "Atualizar audiÃªncia", description = "Atualiza os dados de uma audiÃªncia agendada")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "AudiÃªncia atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de requisiÃ§Ã£o invÃ¡lidos",
                    content = @Content(schema = @Schema(implementation = ErroValidacaoDTO.class))),
            @ApiResponse(responseCode = "401", description = "NÃ£o autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "AudiÃªncia nÃ£o encontrada",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "409", description = "Conflito de integridade",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "422", description = "Regra de negÃ³cio violada",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<AudienciaDTO> atualizar(UUID id, AudienciaDTO dto);

    @Operation(summary = "Excluir audiÃªncia", description = "Remove um agendamento de audiÃªncia")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "AudiÃªncia excluÃ­da com sucesso"),
            @ApiResponse(responseCode = "401", description = "NÃ£o autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "AudiÃªncia nÃ£o encontrada",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "409", description = "Conflito de integridade referencial",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<Void> excluir(UUID id);

    @Operation(summary = "Alterar status da audiÃªncia", description = "Modifica o status de uma audiÃªncia (ex: REALIZADA, CANCELADA)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status alterado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Status informado invÃ¡lido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "401", description = "NÃ£o autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "AudiÃªncia nÃ£o encontrada",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "422", description = "Regra de negÃ³cio violada",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<AudienciaDTO> alterarStatus(UUID id, StatusAudienciaEnum status);

    @Operation(summary = "Gerar e anexar resumo preparatÃ³rio por IA a partir de documentos dos autos",
            description = "Baixa os PDFs anexados no Google Drive, extrai o texto processual via PDFBox e anexa o resumo preparatÃ³rio estruturado Ã  audiÃªncia")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resumo gerado e anexado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Lista de identificadores de documentos invÃ¡lida ou vazia",
                    content = @Content(schema = @Schema(implementation = ErroValidacaoDTO.class))),
            @ApiResponse(responseCode = "401", description = "NÃ£o autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "AudiÃªncia nÃ£o encontrada",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "422", description = "Falha ao processar documentos ou gerar resumo na IA",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<AudienciaDTO> gerarResumoIa(UUID id, GerarResumoAudienciaDTO dto);
}
