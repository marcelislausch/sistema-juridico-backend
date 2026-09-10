package com.sistemajuridico.backend.presentation.openapi;

import com.sistemajuridico.backend.presentation.dtos.ErroPadraoDTO;
import com.sistemajuridico.backend.presentation.dtos.ErroValidacaoDTO;
import com.sistemajuridico.backend.presentation.dtos.EscritorioDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "ConfiguraÃ§Ãµes do EscritÃ³rio", description = "Gerenciamento dos dados cadastrais e institucionais do escritÃ³rio")
public interface EscritorioConfigControllerOpenApi {

    @Operation(summary = "Obter dados do escritÃ³rio", description = "Recupera as informaÃ§Ãµes institucionais, endereÃ§o e contatos do escritÃ³rio")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dados do escritÃ³rio recuperados com sucesso"),
            @ApiResponse(responseCode = "401", description = "NÃ£o autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "ConfiguraÃ§Ã£o do escritÃ³rio nÃ£o encontrada",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<EscritorioDTO> obterConfiguracao();

    @Operation(summary = "Atualizar dados do escritÃ³rio", description = "Atualiza os dados institucionais, endereÃ§o e contatos do escritÃ³rio")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "ConfiguraÃ§Ãµes atualizadas com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados cadastrais invÃ¡lidos",
                    content = @Content(schema = @Schema(implementation = ErroValidacaoDTO.class))),
            @ApiResponse(responseCode = "401", description = "NÃ£o autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "422", description = "Regra de negÃ³cio violada",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<EscritorioDTO> atualizarConfiguracao(EscritorioDTO dto);
}
