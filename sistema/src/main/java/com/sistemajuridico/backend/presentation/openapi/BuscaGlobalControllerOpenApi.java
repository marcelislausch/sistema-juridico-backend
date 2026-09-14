package com.sistemajuridico.backend.presentation.openapi;

import com.sistemajuridico.backend.core.domain.enums.TipoItemBuscaEnum;
import com.sistemajuridico.backend.presentation.dtos.ErroPadraoDTO;
import com.sistemajuridico.backend.presentation.dtos.ItemBuscaDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Busca Global", description = "Pesquisa unificada em múltiplos domínios (Processos, Clientes, Equipe)")
public interface BuscaGlobalControllerOpenApi {

    @Operation(summary = "Realiza pesquisa global textual consolidando resultados de processos, clientes e usuários")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resultados da pesquisa consolidada retornados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetro de busca textual obrigatório não informado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<List<ItemBuscaDTO>> buscar(String q, List<TipoItemBuscaEnum> tipos, int limit);
}
