package com.sistemajuridico.backend.presentation.openapi;

import com.sistemajuridico.backend.presentation.dtos.DocumentoDTO;
import com.sistemajuridico.backend.presentation.dtos.ErroPadraoDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Tag(name = "Documentos", description = "Gestão de arquivos, upload multipart e download de documentos")
public interface DocumentoControllerOpenApi {

    @Operation(summary = "Fazer upload de documento", description = "Envia um novo arquivo multipart vinculado a um processo ou cliente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Documento anexado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Arquivo ausente ou inválido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Cliente ou processo informado não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "422", description = "Regra de negócio violada ao fazer upload",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<DocumentoDTO> upload(MultipartFile arquivo, String titulo, UUID clienteId, UUID processoId);

    @Operation(summary = "Listar documentos por cliente", description = "Recupera os metadados de todos os documentos associados a um cliente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de documentos retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<List<DocumentoDTO>> listarPorCliente(UUID clienteId);

    @Operation(summary = "Listar documentos por processo", description = "Recupera os metadados de todos os documentos associados a um processo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de documentos retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Processo não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<List<DocumentoDTO>> listarPorProcesso(UUID processoId);

    @Operation(summary = "Baixar documento", description = "Realiza o download binário do arquivo armazenado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Download do arquivo iniciado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Documento não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<byte[]> download(UUID id);

    @Operation(summary = "Excluir documento", description = "Remove o documento e exclui o arquivo associado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Documento excluído com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Documento não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "409", description = "Conflito ao excluir arquivo",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<Void> excluir(UUID id);
}
