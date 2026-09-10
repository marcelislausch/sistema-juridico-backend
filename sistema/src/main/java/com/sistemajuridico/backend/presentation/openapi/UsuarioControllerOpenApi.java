package com.sistemajuridico.backend.presentation.openapi;

import com.sistemajuridico.backend.presentation.dtos.CriarUsuarioRequest;
import com.sistemajuridico.backend.presentation.dtos.ErroPadraoDTO;
import com.sistemajuridico.backend.presentation.dtos.ErroValidacaoDTO;
import com.sistemajuridico.backend.presentation.dtos.UsuarioResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

@Tag(name = "UsuÃ¡rios", description = "GestÃ£o de equipe, perfis de acesso e credenciais")
public interface UsuarioControllerOpenApi {

    @Operation(summary = "Listagem paginada dos membros da equipe", description = "Retorna lista paginada de usuÃ¡rios da equipe com filtros por status ativo e termo textual")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PÃ¡gina de usuÃ¡rios retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "ParÃ¢metros de consulta invÃ¡lidos",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "401", description = "NÃ£o autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido para o perfil atual",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<Page<UsuarioResponseDTO>> listar(
            Boolean ativo,
            String q,
            String termoBusca,
            String termoParam,
            Pageable pageable
    );

    @Operation(summary = "Cadastrar novo usuÃ¡rio", description = "Cadastra um novo membro da equipe (ADMIN, ADVOGADO ou SECRETARIA)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "UsuÃ¡rio cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de requisiÃ§Ã£o invÃ¡lidos ou campos nÃ£o atendem as validaÃ§Ãµes",
                    content = @Content(schema = @Schema(implementation = ErroValidacaoDTO.class))),
            @ApiResponse(responseCode = "401", description = "NÃ£o autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "409", description = "Conflito de integridade (ex: e-mail jÃ¡ cadastrado)",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "422", description = "Regra de negÃ³cio violada",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<UsuarioResponseDTO> criar(CriarUsuarioRequest request);

    @Operation(summary = "Buscar usuÃ¡rio por ID", description = "Recupera dados detalhados de um membro da equipe")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "UsuÃ¡rio recuperado com sucesso"),
            @ApiResponse(responseCode = "401", description = "NÃ£o autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "UsuÃ¡rio nÃ£o encontrado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<UsuarioResponseDTO> buscarPorId(UUID id);

    @Operation(summary = "Listar todos os advogados", description = "Retorna lista de advogados ativos cadastrados no escritÃ³rio para seleÃ§Ã£o de responsÃ¡veis")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de advogados retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "NÃ£o autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<List<UsuarioResponseDTO>> listarAdvogados();
}
