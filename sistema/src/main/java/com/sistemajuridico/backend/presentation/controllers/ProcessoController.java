package com.sistemajuridico.backend.presentation.controllers;

import com.sistemajuridico.backend.core.domain.Processo;
import com.sistemajuridico.backend.core.domain.enums.FaseProcessualEnum;
import com.sistemajuridico.backend.core.usecases.*;
import com.sistemajuridico.backend.presentation.dtos.ErroPadraoDTO;
import com.sistemajuridico.backend.presentation.dtos.ErroValidacaoDTO;
import com.sistemajuridico.backend.presentation.dtos.ProcessoDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/processos")
@Tag(name = "Processos", description = "Gestão de processos judiciais, fases processuais e arquivamento")
public class ProcessoController {

    private final CadastrarProcessoUseCase cadastrarProcessoUseCase;
    private final ListarProcessosUseCase listarProcessosUseCase;
    private final BuscarProcessoPorIdUseCase buscarProcessoPorIdUseCase;
    private final ListarProcessosPorClienteUseCase listarProcessosPorClienteUseCase;
    private final ArquivarProcessoUseCase arquivarProcessoUseCase;
    private final AtualizarProcessoUseCase atualizarProcessoUseCase;
    private final DesarquivarProcessoUseCase desarquivarProcessoUseCase;

    public ProcessoController(CadastrarProcessoUseCase cadastrarProcessoUseCase,
                              ListarProcessosUseCase listarProcessosUseCase,
                              BuscarProcessoPorIdUseCase buscarProcessoPorIdUseCase,
                              ListarProcessosPorClienteUseCase listarProcessosPorClienteUseCase,
                              ArquivarProcessoUseCase arquivarProcessoUseCase,
                              AtualizarProcessoUseCase atualizarProcessoUseCase,
                              DesarquivarProcessoUseCase desarquivarProcessoUseCase) {
        this.cadastrarProcessoUseCase = cadastrarProcessoUseCase;
        this.listarProcessosUseCase = listarProcessosUseCase;
        this.buscarProcessoPorIdUseCase = buscarProcessoPorIdUseCase;
        this.listarProcessosPorClienteUseCase = listarProcessosPorClienteUseCase;
        this.arquivarProcessoUseCase = arquivarProcessoUseCase;
        this.atualizarProcessoUseCase = atualizarProcessoUseCase;
        this.desarquivarProcessoUseCase = desarquivarProcessoUseCase;
    }

    @PostMapping
    @Operation(summary = "Cadastrar novo processo", description = "Cadastra um novo processo judicial vinculado a um cliente e advogado responsável")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Processo cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de requisição inválidos ou campos obrigatórios ausentes",
                    content = @Content(schema = @Schema(implementation = ErroValidacaoDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Cliente ou advogado informado não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "409", description = "Conflito de integridade (ex: número CNJ já existente)",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "422", description = "Regra de negócio violada",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    public ResponseEntity<ProcessoDTO> criar(@RequestBody @Valid ProcessoDTO dto) {
        Processo processo = dto.toEntity();
        Processo processoSalvo = cadastrarProcessoUseCase.executar(processo, dto.clienteId(), dto.advogadoId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ProcessoDTO.fromEntity(processoSalvo));
    }

    @GetMapping
    @Operation(summary = "Listar processos com paginação e filtros", description = "Consulta paginada de processos com suporte a filtros dinâmicos por termo, fase, cliente, advogado e status de arquivamento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Página de processos retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de consulta inválidos",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    public ResponseEntity<Page<ProcessoDTO>> listar(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "termoBusca", required = false) String termoBusca,
            @RequestParam(name = "termo", required = false) String termoParam,
            @RequestParam(name = "fase", required = false) FaseProcessualEnum fase,
            @RequestParam(name = "arquivado", required = false) Boolean arquivado,
            @RequestParam(name = "clienteId", required = false) UUID clienteId,
            @RequestParam(name = "advogadoId", required = false) UUID advogadoId,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {
        String termo = null;
        if (q != null && !q.trim().isEmpty()) {
            termo = q.trim();
        } else if (termoBusca != null && !termoBusca.trim().isEmpty()) {
            termo = termoBusca.trim();
        } else if (termoParam != null && !termoParam.trim().isEmpty()) {
            termo = termoParam.trim();
        }

        Page<Processo> paginaProcessos = this.listarProcessosUseCase.executar(termo, fase, arquivado, clienteId, advogadoId, pageable);
        List<ProcessoDTO> dtos = new ArrayList<>();
        for (Processo processo : paginaProcessos.getContent()) {
            dtos.add(ProcessoDTO.fromEntity(processo));
        }
        Page<ProcessoDTO> pageDtos = new PageImpl<>(dtos, paginaProcessos.getPageable(), paginaProcessos.getTotalElements());
        return ResponseEntity.ok(pageDtos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar processo por ID", description = "Recupera os detalhes completos de um processo através do seu identificador único")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Processo encontrado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Processo não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    public ResponseEntity<ProcessoDTO> buscarPorId(@PathVariable UUID id) {
        Processo processo = this.buscarProcessoPorIdUseCase.executar(id);
        return ResponseEntity.ok(ProcessoDTO.fromEntity(processo));
    }

    @GetMapping("/cliente/{clienteId}")
    @Operation(summary = "Listar processos por cliente", description = "Retorna os processos vinculados a um cliente de forma paginada")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Processos do cliente retornados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    public ResponseEntity<Page<ProcessoDTO>> listarPorCliente(
            @PathVariable UUID clienteId,
            @ParameterObject Pageable pageable) {
        Page<Processo> paginaProcessos = listarProcessosPorClienteUseCase.executar(clienteId, pageable);
        List<ProcessoDTO> dtoList = new ArrayList<>();
        for (Processo processo : paginaProcessos.getContent()) {
            dtoList.add(ProcessoDTO.fromEntity(processo));
        }
        Page<ProcessoDTO> response = new PageImpl<>(dtoList, pageable, paginaProcessos.getTotalElements());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar processo", description = "Atualiza os dados de um processo judicial existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Processo atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de requisição inválidos ou campos incorretos",
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
    public ResponseEntity<ProcessoDTO> atualizar(@PathVariable UUID id, @RequestBody @Valid ProcessoDTO dto) {
        Processo processo = dto.toEntity();
        Processo processoAtualizado = this.atualizarProcessoUseCase.executar(id, processo);
        return ResponseEntity.ok(ProcessoDTO.fromEntity(processoAtualizado));
    }

    @PatchMapping("/{id}/arquivar")
    @Operation(summary = "Arquivar processo", description = "Marca o processo judicial como arquivado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Processo arquivado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Processo não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "422", description = "Regra de negócio violada ao arquivar processo",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    public ResponseEntity<ProcessoDTO> arquivar(@PathVariable UUID id) {
        Processo processoArquivado = arquivarProcessoUseCase.executar(id);
        return ResponseEntity.ok(ProcessoDTO.fromEntity(processoArquivado));
    }

    @PatchMapping("/{id}/desarquivar")
    @Operation(summary = "Desarquivar processo", description = "Restaura o processo judicial arquivado para atividade")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Processo desarquivado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Processo não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "422", description = "Regra de negócio violada ao desarquivar processo",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    public ResponseEntity<ProcessoDTO> desarquivar(@PathVariable UUID id) {
        Processo processoDesarquivado = this.desarquivarProcessoUseCase.executar(id);
        return ResponseEntity.ok(ProcessoDTO.fromEntity(processoDesarquivado));
    }
}
