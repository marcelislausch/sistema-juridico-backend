package com.sistemajuridico.backend.presentation.controllers;

import com.sistemajuridico.backend.core.domain.Processo;
import com.sistemajuridico.backend.core.usecases.ArquivarProcessoUseCase;
import com.sistemajuridico.backend.core.usecases.CadastrarProcessoUseCase;
import com.sistemajuridico.backend.core.usecases.ListarProcessosPorClienteUseCase;
import com.sistemajuridico.backend.presentation.dtos.ProcessoDTO;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
    private final ListarProcessosPorClienteUseCase listarProcessosPorClienteUseCase;
    private final ArquivarProcessoUseCase arquivarProcessoUseCase;

    public ProcessoController(CadastrarProcessoUseCase cadastrarProcessoUseCase,
                              ListarProcessosPorClienteUseCase listarProcessosPorClienteUseCase,
                              ArquivarProcessoUseCase arquivarProcessoUseCase) {
        this.cadastrarProcessoUseCase = cadastrarProcessoUseCase;
        this.listarProcessosPorClienteUseCase = listarProcessosPorClienteUseCase;
        this.arquivarProcessoUseCase = arquivarProcessoUseCase;
    }

    @PostMapping
    public ResponseEntity<ProcessoDTO> criar(@RequestBody @Valid ProcessoDTO dto) {
        Processo processo = dto.toEntity();
        Processo processoSalvo = cadastrarProcessoUseCase.executar(processo, dto.clienteId(), dto.advogadoId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ProcessoDTO.fromEntity(processoSalvo));
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

    @PatchMapping("/{id}/arquivar")
    public ResponseEntity<ProcessoDTO> arquivar(@PathVariable UUID id) {
        Processo processoArquivado = arquivarProcessoUseCase.executar(id);
        return ResponseEntity.ok(ProcessoDTO.fromEntity(processoArquivado));
    }
}
