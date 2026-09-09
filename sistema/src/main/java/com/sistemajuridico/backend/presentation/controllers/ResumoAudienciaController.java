package com.sistemajuridico.backend.presentation.controllers;

import com.sistemajuridico.backend.core.usecases.GerarResumoAudienciaUseCase;
import com.sistemajuridico.backend.presentation.dtos.ErroPadraoDTO;
import com.sistemajuridico.backend.presentation.dtos.ErroValidacaoDTO;
import com.sistemajuridico.backend.presentation.dtos.ResumoPecaDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ia/resumos")
@Tag(name = "Inteligência Artificial", description = "Serviços de inteligência artificial e geração de resumos para peças e audiências")
public class ResumoAudienciaController {

    private final GerarResumoAudienciaUseCase gerarResumoAudienciaUseCase;

    public ResumoAudienciaController(GerarResumoAudienciaUseCase gerarResumoAudienciaUseCase) {
        this.gerarResumoAudienciaUseCase = gerarResumoAudienciaUseCase;
    }

    @PostMapping("/audiencia")
    @Operation(summary = "Gerar resumo preparatório de audiência via IA", description = "Gera um resumo estruturado e pontos de atenção com base no conteúdo da peça processual informada")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resumo gerado com sucesso pela inteligência artificial"),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos ou peça vazia",
                    content = @Content(schema = @Schema(implementation = ErroValidacaoDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "422", description = "Falha ao processar resumo pelo modelo de IA",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    public ResponseEntity<String> resumirParaAudiencia(@RequestBody @Valid ResumoPecaDTO dto) {
        String resumo = this.gerarResumoAudienciaUseCase.executar(dto.conteudoPeca());
        return ResponseEntity.ok(resumo);
    }
}
