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

@Tag(name = "Audiências", description = "Gestão e agendamento de audiências judiciais e resumos preparatórios via IA")
public interface AudienciaControllerOpenApi {

    @Operation(summary = "Agendar nova audiência", description = "Cadastra uma nova audiência associada a um processo judicial")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Audiência agendada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de requisição inválidos ou campos obrigatórios ausentes",
                    content = @Content(schema = @Schema(implementation = ErroValidacaoDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Processo ou responsável informado não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "409", description = "Conflito de integridade",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "422", description = "Regra de negócio violada",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<AudienciaDTO> criar(AudienciaDTO dto);

    @Operation(summary = "Listar agenda de audiências", description = "Retorna audiências agendadas com filtros por período, status, processo e responsável")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Agenda de audiências retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de consulta inválidos",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
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

    @Operation(summary = "Listar audiências por processo", description = "Recupera todas as audiências vinculadas a um processo específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de audiências retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Processo não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<List<AudienciaDTO>> listarPorProcesso(UUID processoId);

    @Operation(summary = "Buscar audiência por ID", description = "Recupera os detalhes de uma audiência pelo identificador único")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Audiência retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Audiência não encontrada",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<AudienciaDTO> buscarPorId(UUID id);

    @Operation(summary = "Atualizar audiência", description = "Atualiza os dados de uma audiência agendada")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Audiência atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de requisição inválidos",
                    content = @Content(schema = @Schema(implementation = ErroValidacaoDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Audiência não encontrada",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "409", description = "Conflito de integridade",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "422", description = "Regra de negócio violada",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<AudienciaDTO> atualizar(UUID id, AudienciaDTO dto);

    @Operation(summary = "Excluir audiência", description = "Remove um agendamento de audiência")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Audiência excluída com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Audiência não encontrada",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "409", description = "Conflito de integridade referencial",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<Void> excluir(UUID id);

    @Operation(summary = "Alterar status da audiência", description = "Modifica o status de uma audiência (ex: REALIZADA, CANCELADA)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status alterado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Status informado inválido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Audiência não encontrada",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "422", description = "Regra de negócio violada",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<AudienciaDTO> alterarStatus(UUID id, StatusAudienciaEnum status);

    @Operation(summary = "Gerar e anexar resumo preparatório por IA a partir de documentos dos autos",
            description = "Baixa os PDFs anexados no Google Drive, extrai o texto processual via PDFBox e anexa o resumo preparatório estruturado à audiência")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resumo gerado e anexado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Lista de identificadores de documentos inválida ou vazia",
                    content = @Content(schema = @Schema(implementation = ErroValidacaoDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Audiência não encontrada",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "422", description = "Falha ao processar documentos ou gerar resumo na IA",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<AudienciaDTO> gerarResumoIa(UUID id, GerarResumoAudienciaDTO dto);
}
