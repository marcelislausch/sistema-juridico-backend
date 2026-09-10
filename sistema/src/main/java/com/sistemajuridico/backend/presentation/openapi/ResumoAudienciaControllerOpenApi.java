package com.sistemajuridico.backend.presentation.openapi;

import com.sistemajuridico.backend.core.domain.dto.ResumoAudienciaEstruturadoDTO;
import com.sistemajuridico.backend.presentation.dtos.ErroPadraoDTO;
import com.sistemajuridico.backend.presentation.dtos.ErroValidacaoDTO;
import com.sistemajuridico.backend.presentation.dtos.ResumoPecaDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "InteligÃªncia Artificial", description = "ServiÃ§os de inteligÃªncia artificial e geraÃ§Ã£o de resumos para peÃ§as e audiÃªncias")
public interface ResumoAudienciaControllerOpenApi {

    @Operation(summary = "Gerar resumo preparatÃ³rio de audiÃªncia via IA a partir de documentos dos autos",
            description = "Baixa os PDFs anexados no Google Drive, extrai o texto processual e gera um dossiÃª tÃ¡tico estruturado com fatos incontroversos, fatos controvertidos, riscos processuais, roteiro de perguntas e parÃ¢metros de acordo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resumo estruturado gerado com sucesso pela inteligÃªncia artificial",
                    content = @Content(schema = @Schema(implementation = ResumoAudienciaEstruturadoDTO.class))),
            @ApiResponse(responseCode = "400", description = "Lista de identificadores de documentos invÃ¡lida ou vazia",
                    content = @Content(schema = @Schema(implementation = ErroValidacaoDTO.class))),
            @ApiResponse(responseCode = "401", description = "NÃ£o autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "422", description = "Falha ao baixar documentos ou processar resumo pelo modelo de IA",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<ResumoAudienciaEstruturadoDTO> resumirParaAudiencia(ResumoPecaDTO dto);
}
