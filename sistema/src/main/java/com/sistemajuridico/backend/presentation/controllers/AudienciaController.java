package com.sistemajuridico.backend.presentation.controllers;

import com.sistemajuridico.backend.core.domain.Audiencia;
import com.sistemajuridico.backend.core.domain.enums.StatusAudienciaEnum;
import com.sistemajuridico.backend.core.usecases.*;
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
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/audiencias")
@Tag(name = "Audiências", description = "Gestão e agendamento de audiências judiciais e resumos preparatórios via IA")
public class AudienciaController {

    private final CadastrarAudienciaUseCase cadastrarAudienciaUseCase;
    private final ListarAudienciasPorProcessoUseCase listarAudienciasPorProcessoUseCase;
    private final AlterarStatusAudienciaUseCase alterarStatusAudienciaUseCase;
    private final ListarAgendaGlobalUseCase listarAgendaGlobalUseCase;
    private final GerarEAnexarResumoAudienciaUseCase gerarEAnexarResumoAudienciaUseCase;
    private final BuscarAudienciaPorIdUseCase buscarAudienciaPorIdUseCase;
    private final AtualizarAudienciaUseCase atualizarAudienciaUseCase;
    private final ExcluirAudienciaUseCase excluirAudienciaUseCase;

    public AudienciaController(CadastrarAudienciaUseCase cadastrarAudienciaUseCase,
                               ListarAudienciasPorProcessoUseCase listarAudienciasPorProcessoUseCase,
                               AlterarStatusAudienciaUseCase alterarStatusAudienciaUseCase,
                               ListarAgendaGlobalUseCase listarAgendaGlobalUseCase,
                               GerarEAnexarResumoAudienciaUseCase gerarEAnexarResumoAudienciaUseCase,
                               BuscarAudienciaPorIdUseCase buscarAudienciaPorIdUseCase,
                               AtualizarAudienciaUseCase atualizarAudienciaUseCase,
                               ExcluirAudienciaUseCase excluirAudienciaUseCase) {
        this.cadastrarAudienciaUseCase = cadastrarAudienciaUseCase;
        this.listarAudienciasPorProcessoUseCase = listarAudienciasPorProcessoUseCase;
        this.alterarStatusAudienciaUseCase = alterarStatusAudienciaUseCase;
        this.listarAgendaGlobalUseCase = listarAgendaGlobalUseCase;
        this.gerarEAnexarResumoAudienciaUseCase = gerarEAnexarResumoAudienciaUseCase;
        this.buscarAudienciaPorIdUseCase = buscarAudienciaPorIdUseCase;
        this.atualizarAudienciaUseCase = atualizarAudienciaUseCase;
        this.excluirAudienciaUseCase = excluirAudienciaUseCase;
    }

    @PostMapping
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
    public ResponseEntity<AudienciaDTO> criar(@RequestBody @Valid AudienciaDTO dto) {
        Audiencia audiencia = dto.toEntity();
        Audiencia audienciaSalva = cadastrarAudienciaUseCase.executar(audiencia, dto.processoId());
        return ResponseEntity.status(HttpStatus.CREATED).body(AudienciaDTO.fromEntity(audienciaSalva));
    }

    @GetMapping("/agenda")
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
    public ResponseEntity<List<AudienciaDTO>> listarAgenda(
            @RequestParam(name = "inicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(name = "fim", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(name = "status", required = false) StatusAudienciaEnum status,
            @RequestParam(name = "processoId", required = false) UUID processoId,
            @RequestParam(name = "responsavelId", required = false) UUID responsavelId) {
        LocalDateTime inicioDia = null;
        if (inicio != null) {
            inicioDia = inicio.atStartOfDay();
        }
        LocalDateTime fimDia = null;
        if (fim != null) {
            fimDia = fim.atTime(23, 59, 59);
        }
        List<Audiencia> audiencias = listarAgendaGlobalUseCase.executar(inicioDia, fimDia, status, processoId, responsavelId);
        List<AudienciaDTO> response = new ArrayList<>();
        for (Audiencia audiencia : audiencias) {
            response.add(AudienciaDTO.fromEntity(audiencia));
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/processo/{processoId}")
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
    public ResponseEntity<List<AudienciaDTO>> listarPorProcesso(@PathVariable UUID processoId) {
        List<Audiencia> audiencias = listarAudienciasPorProcessoUseCase.executar(processoId);
        List<AudienciaDTO> response = new ArrayList<>();
        for (Audiencia audiencia : audiencias) {
            response.add(AudienciaDTO.fromEntity(audiencia));
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
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
    public ResponseEntity<AudienciaDTO> buscarPorId(@PathVariable UUID id) {
        Audiencia audiencia = this.buscarAudienciaPorIdUseCase.executar(id);
        return ResponseEntity.ok(AudienciaDTO.fromEntity(audiencia));
    }

    @PutMapping("/{id}")
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
    public ResponseEntity<AudienciaDTO> atualizar(@PathVariable UUID id, @RequestBody @Valid AudienciaDTO dto) {
        Audiencia audiencia = dto.toEntity();
        Audiencia audienciaAtualizada = this.atualizarAudienciaUseCase.executar(id, audiencia);
        return ResponseEntity.ok(AudienciaDTO.fromEntity(audienciaAtualizada));
    }

    @DeleteMapping("/{id}")
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
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        this.excluirAudienciaUseCase.executar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
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
    public ResponseEntity<AudienciaDTO> alterarStatus(@PathVariable UUID id, @RequestParam StatusAudienciaEnum status) {
        Audiencia audienciaAtualizada = alterarStatusAudienciaUseCase.executar(id, status);
        return ResponseEntity.ok(AudienciaDTO.fromEntity(audienciaAtualizada));
    }

    @PostMapping("/{id}/gerar-resumo-ia")
    @Operation(summary = "Gerar e anexar resumo preparatório por IA", description = "Processa o texto da peça processual via IA e anexa o resumo preparatório à audiência")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resumo gerado e anexado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Conteúdo da peça inválido ou vazio",
                    content = @Content(schema = @Schema(implementation = ErroValidacaoDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Audiência não encontrada",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "422", description = "Falha ao gerar resumo na IA",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    public ResponseEntity<AudienciaDTO> gerarResumoIa(@PathVariable UUID id, @RequestBody @Valid GerarResumoAudienciaDTO dto) {
        Audiencia audienciaAtualizada = this.gerarEAnexarResumoAudienciaUseCase.executar(id, dto.conteudoPeca());
        return ResponseEntity.ok(AudienciaDTO.fromEntity(audienciaAtualizada));
    }
}
