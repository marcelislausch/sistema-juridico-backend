package com.sistemajuridico.backend.presentation.controllers;

import com.sistemajuridico.backend.core.service.EscritorioService;
import com.sistemajuridico.backend.presentation.dtos.ErroPadraoDTO;
import com.sistemajuridico.backend.presentation.dtos.ErroValidacaoDTO;
import com.sistemajuridico.backend.presentation.dtos.EscritorioDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/configuracoes/escritorio")
@Tag(name = "Configurações do Escritório", description = "Gerenciamento dos dados cadastrais e institucionais do escritório")
public class EscritorioConfigController {

    private final EscritorioService escritorioService;

    public EscritorioConfigController(EscritorioService escritorioService) {
        this.escritorioService = escritorioService;
    }

    @GetMapping
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
    public ResponseEntity<EscritorioDTO> obterConfiguracao() {
        EscritorioDTO dto = this.escritorioService.obterDadosEscritorio();
        return ResponseEntity.ok(dto);
    }

    @PutMapping
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
    public ResponseEntity<EscritorioDTO> atualizarConfiguracao(@RequestBody @Valid EscritorioDTO dto) {
        EscritorioDTO atualizado = this.escritorioService.atualizarDadosEscritorio(dto);
        return ResponseEntity.ok(atualizado);
    }
}
