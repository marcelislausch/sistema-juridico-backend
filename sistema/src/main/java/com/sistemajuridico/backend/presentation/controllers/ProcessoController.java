package com.sistemajuridico.backend.presentation.controllers;

import com.sistemajuridico.backend.core.domain.Processo;
import com.sistemajuridico.backend.core.domain.enums.FaseProcessualEnum;
import com.sistemajuridico.backend.core.usecases.*;
import com.sistemajuridico.backend.presentation.dtos.ProcessoDTO;
import jakarta.validation.Valid;
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
    public ResponseEntity<ProcessoDTO> criar(@RequestBody @Valid ProcessoDTO dto) {
        Processo processo = dto.toEntity();
        Processo processoSalvo = cadastrarProcessoUseCase.executar(processo, dto.clienteId(), dto.advogadoId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ProcessoDTO.fromEntity(processoSalvo));
    }

    @GetMapping
    public ResponseEntity<Page<ProcessoDTO>> listar(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "termoBusca", required = false) String termoBusca,
            @RequestParam(name = "termo", required = false) String termoParam,
            @RequestParam(name = "fase", required = false) FaseProcessualEnum fase,
            @RequestParam(name = "arquivado", required = false) Boolean arquivado,
            @RequestParam(name = "clienteId", required = false) UUID clienteId,
            @RequestParam(name = "advogadoId", required = false) UUID advogadoId,
            @PageableDefault(size = 10) Pageable pageable) {
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
    public ResponseEntity<ProcessoDTO> buscarPorId(@PathVariable UUID id) {
        Processo processo = this.buscarProcessoPorIdUseCase.executar(id);
        return ResponseEntity.ok(ProcessoDTO.fromEntity(processo));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<Page<ProcessoDTO>> listarPorCliente(@PathVariable UUID clienteId, Pageable pageable) {
        Page<Processo> paginaProcessos = listarProcessosPorClienteUseCase.executar(clienteId, pageable);
        List<ProcessoDTO> dtoList = new ArrayList<>();
        for (Processo processo : paginaProcessos.getContent()) {
            dtoList.add(ProcessoDTO.fromEntity(processo));
        }
        Page<ProcessoDTO> response = new PageImpl<>(dtoList, pageable, paginaProcessos.getTotalElements());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProcessoDTO> atualizar(@PathVariable UUID id, @RequestBody @Valid ProcessoDTO dto) {
        Processo processo = dto.toEntity();
        Processo processoAtualizado = this.atualizarProcessoUseCase.executar(id, processo);
        return ResponseEntity.ok(ProcessoDTO.fromEntity(processoAtualizado));
    }

    @PatchMapping("/{id}/arquivar")
    public ResponseEntity<ProcessoDTO> arquivar(@PathVariable UUID id) {
        Processo processoArquivado = arquivarProcessoUseCase.executar(id);
        return ResponseEntity.ok(ProcessoDTO.fromEntity(processoArquivado));
    }

    @PatchMapping("/{id}/desarquivar")
    public ResponseEntity<ProcessoDTO> desarquivar(@PathVariable UUID id) {
        Processo processoDesarquivado = this.desarquivarProcessoUseCase.executar(id);
        return ResponseEntity.ok(ProcessoDTO.fromEntity(processoDesarquivado));
    }
}
