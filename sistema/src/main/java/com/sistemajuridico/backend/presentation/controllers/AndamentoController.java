package com.sistemajuridico.backend.presentation.controllers;

import com.sistemajuridico.backend.core.domain.Andamento;
import com.sistemajuridico.backend.core.usecases.CadastrarAndamentoUseCase;
import com.sistemajuridico.backend.core.usecases.ListarAndamentosPorProcessoUseCase;
import com.sistemajuridico.backend.presentation.dtos.AndamentoDTO;
import com.sistemajuridico.backend.presentation.dtos.ErroPadraoDTO;
import com.sistemajuridico.backend.presentation.dtos.ErroValidacaoDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/processos")
@Tag(name = "Andamentos", description = "Registro e acompanhamento de andamentos processuais")
public class AndamentoController {

    private final CadastrarAndamentoUseCase cadastrarAndamentoUseCase;
    private final ListarAndamentosPorProcessoUseCase listarAndamentosPorProcessoUseCase;

    public AndamentoController(CadastrarAndamentoUseCase cadastrarAndamentoUseCase,
                               ListarAndamentosPorProcessoUseCase listarAndamentosPorProcessoUseCase) {
        this.cadastrarAndamentoUseCase = cadastrarAndamentoUseCase;
        this.listarAndamentosPorProcessoUseCase = listarAndamentosPorProcessoUseCase;
    }

    @PostMapping("/{processoId}/andamentos")
    @Operation(summary = "Cadastrar andamento processual", description = "Registra um novo andamento ou movimentação vinculada a um processo judicial")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Andamento cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de requisição inválidos",
                    content = @Content(schema = @Schema(implementation = ErroValidacaoDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Processo não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "409", description = "Conflito de integridade",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "422", description = "Regra de negócio violada",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    public ResponseEntity<AndamentoDTO> criar(@PathVariable UUID processoId, @RequestBody @Valid AndamentoDTO dto) {
        Andamento andamento = dto.toEntity();
        Andamento andamentoSalvo = cadastrarAndamentoUseCase.executar(andamento, processoId);
        return ResponseEntity.status(HttpStatus.CREATED).body(AndamentoDTO.fromEntity(andamentoSalvo));
    }

    @GetMapping("/{processoId}/andamentos")
    @Operation(summary = "Listar andamentos por processo", description = "Recupera o histórico cronológico de andamentos do processo informado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de andamentos retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Processo não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    public ResponseEntity<List<AndamentoDTO>> listarPorProcesso(@PathVariable UUID processoId) {
        List<Andamento> andamentos = listarAndamentosPorProcessoUseCase.executar(processoId);
        List<AndamentoDTO> response = new ArrayList<>();
        for (Andamento andamento : andamentos) {
            response.add(AndamentoDTO.fromEntity(andamento));
        }

        return ResponseEntity.ok(response);
    }
}
