package com.sistemajuridico.backend.presentation.controllers;

import com.sistemajuridico.backend.core.domain.Tarefa;
import com.sistemajuridico.backend.core.domain.Usuario;
import com.sistemajuridico.backend.core.domain.enums.TipoTarefaEnum;
import com.sistemajuridico.backend.core.domain.exceptions.RecursoNaoEncontradoException;
import com.sistemajuridico.backend.core.domain.exceptions.RegraNegocioException;
import com.sistemajuridico.backend.core.usecases.*;
import com.sistemajuridico.backend.infrastructure.persistence.UsuarioRepository;
import com.sistemajuridico.backend.presentation.dtos.ErroPadraoDTO;
import com.sistemajuridico.backend.presentation.dtos.ErroValidacaoDTO;
import com.sistemajuridico.backend.presentation.dtos.TarefaDTO;
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

import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/tarefas")
@Tag(name = "Tarefas", description = "Gestão de tarefas internas, agenda operacional e prazos")
public class TarefaController {

    private final CriarTarefaUseCase criarTarefaUseCase;
    private final ConcluirTarefaUseCase concluirTarefaUseCase;
    private final AtualizarTarefaUseCase atualizarTarefaUseCase;
    private final ExcluirTarefaUseCase excluirTarefaUseCase;
    private final ListarTarefasDashboardUseCase listarTarefasDashboardUseCase;
    private final ListarTarefasPorPeriodoUseCase listarTarefasPorPeriodoUseCase;
    private final UsuarioRepository usuarioRepository;

    public TarefaController(CriarTarefaUseCase criarTarefaUseCase,
                            ConcluirTarefaUseCase concluirTarefaUseCase,
                            AtualizarTarefaUseCase atualizarTarefaUseCase,
                            ExcluirTarefaUseCase excluirTarefaUseCase,
                            ListarTarefasDashboardUseCase listarTarefasDashboardUseCase,
                            ListarTarefasPorPeriodoUseCase listarTarefasPorPeriodoUseCase,
                            UsuarioRepository usuarioRepository) {
        this.criarTarefaUseCase = criarTarefaUseCase;
        this.concluirTarefaUseCase = concluirTarefaUseCase;
        this.atualizarTarefaUseCase = atualizarTarefaUseCase;
        this.excluirTarefaUseCase = excluirTarefaUseCase;
        this.listarTarefasDashboardUseCase = listarTarefasDashboardUseCase;
        this.listarTarefasPorPeriodoUseCase = listarTarefasPorPeriodoUseCase;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping
    @Operation(summary = "Criar nova tarefa", description = "Cadastra uma nova tarefa atribuída a um responsável e opcionalmente vinculada a um processo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tarefa criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de requisição inválidos ou campos obrigatórios ausentes",
                    content = @Content(schema = @Schema(implementation = ErroValidacaoDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuário responsável ou processo informado não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "409", description = "Conflito de integridade",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "422", description = "Regra de negócio violada",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    public ResponseEntity<TarefaDTO> criar(@RequestBody @Valid TarefaDTO dto) {
        Tarefa tarefa = dto.toEntity();
        Tarefa tarefaSalva = this.criarTarefaUseCase.executar(tarefa, dto.usuarioId(), dto.processoId());
        return ResponseEntity.status(HttpStatus.CREATED).body(TarefaDTO.fromEntity(tarefaSalva));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar tarefa", description = "Atualiza os dados de uma tarefa existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tarefa atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de requisição inválidos",
                    content = @Content(schema = @Schema(implementation = ErroValidacaoDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Tarefa não encontrada",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "409", description = "Conflito de integridade",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "422", description = "Regra de negócio violada",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    public ResponseEntity<TarefaDTO> atualizar(@PathVariable UUID id, @RequestBody @Valid TarefaDTO dto) {
        Tarefa tarefa = dto.toEntity();
        Tarefa tarefaAtualizada = this.atualizarTarefaUseCase.executar(id, tarefa);
        return ResponseEntity.ok(TarefaDTO.fromEntity(tarefaAtualizada));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir tarefa", description = "Remove uma tarefa do sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Tarefa excluída com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Tarefa não encontrada",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "409", description = "Conflito de integridade referencial",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        this.excluirTarefaUseCase.executar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/concluir")
    @Operation(summary = "Concluir tarefa", description = "Marca a tarefa especificada como concluída")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tarefa marcada como concluída com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Tarefa não encontrada",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "422", description = "Regra de negócio violada",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    public ResponseEntity<TarefaDTO> concluir(@PathVariable UUID id) {
        Tarefa tarefaConcluida = this.concluirTarefaUseCase.executar(id);
        return ResponseEntity.ok(TarefaDTO.fromEntity(tarefaConcluida));
    }

    @GetMapping("/dashboard/{usuarioId}")
    @Operation(summary = "Listar tarefas para o dashboard", description = "Recupera tarefas prioritárias pendentes para o painel do usuário informado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tarefas do dashboard recuperadas com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    public ResponseEntity<List<TarefaDTO>> listarDashboard(@PathVariable UUID usuarioId) {
        List<Tarefa> tarefas = this.listarTarefasDashboardUseCase.executar(usuarioId);
        List<TarefaDTO> response = new ArrayList<>();
        for (Tarefa tarefa : tarefas) {
            response.add(TarefaDTO.fromEntity(tarefa));
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/agenda")
    @Operation(summary = "Listar agenda de tarefas", description = "Consulta tarefas por intervalo de datas, status de conclusão, tipo, processo e responsável")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de tarefas da agenda retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de consulta inválidos",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuário da sessão não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "422", description = "Regra de negócio violada",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    public ResponseEntity<List<TarefaDTO>> listarAgenda(
            @RequestParam(name = "inicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(name = "fim", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(name = "concluida", required = false) Boolean concluida,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "tipo", required = false) TipoTarefaEnum tipo,
            @RequestParam(name = "processoId", required = false) UUID processoId,
            @RequestParam(name = "responsavelId", required = false) UUID responsavelId,
            Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new RegraNegocioException("Usuário não autenticado no sistema!");
        }

        String email = principal.getName();
        Optional<Usuario> optUsuario = this.usuarioRepository.findByEmail(email);
        if (optUsuario.isEmpty()) {
            throw new RecursoNaoEncontradoException("Usuário autenticado não encontrado no sistema!");
        }

        Boolean filtroConcluida = concluida;
        if (filtroConcluida == null && status != null) {
            if ("CONCLUIDA".equalsIgnoreCase(status) || "CONCLUIDO".equalsIgnoreCase(status)) {
                filtroConcluida = Boolean.TRUE;
            } else if ("PENDENTE".equalsIgnoreCase(status)) {
                filtroConcluida = Boolean.FALSE;
            }
        }

        Usuario usuarioLogado = optUsuario.get();
        List<TarefaDTO> response = this.listarTarefasPorPeriodoUseCase.executar(
                usuarioLogado, inicio, fim, filtroConcluida, tipo, processoId, responsavelId
        );
        return ResponseEntity.ok(response);
    }
}
