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

@Tag(name = "Configurações do Escritório", description = "Gerenciamento dos dados cadastrais e institucionais do escritório")
public interface EscritorioConfigControllerOpenApi {

    @Operation(summary = "Obter dados do escritório", description = "Recupera as informações institucionais, endereço e contatos do escritório")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dados do escritório recuperados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Configuração do escritório não encontrada",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<EscritorioDTO> obterConfiguracao();

    @Operation(summary = "Atualizar dados do escritório", description = "Atualiza os dados institucionais, endereço e contatos do escritório")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configurações atualizadas com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados cadastrais inválidos",
                    content = @Content(schema = @Schema(implementation = ErroValidacaoDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "422", description = "Regra de negócio violada",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<EscritorioDTO> atualizarConfiguracao(EscritorioDTO dto);
}
