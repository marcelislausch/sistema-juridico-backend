package com.juridia.sistema.presentation.controllers;

import com.juridia.sistema.core.domain.Processo;
import com.juridia.sistema.core.usecases.CadastrarProcessoUseCase;
import com.juridia.sistema.core.usecases.ListarProcessosPorClienteUseCase;
import com.juridia.sistema.presentation.dtos.ProcessoDTO;
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

    public ProcessoController(CadastrarProcessoUseCase cadastrarProcessoUseCase,
                              ListarProcessosPorClienteUseCase listarProcessosPorClienteUseCase) {
        this.cadastrarProcessoUseCase = cadastrarProcessoUseCase;
        this.listarProcessosPorClienteUseCase = listarProcessosPorClienteUseCase;
    }

    @PostMapping
    public ResponseEntity<ProcessoDTO> criar(@RequestBody ProcessoDTO dto) {
        Processo processo = dto.toEntity();
        //Manda para a regra de negócio (passando o clienteId separado para o Use Case buscar no banco)
        Processo processoSalvo = cadastrarProcessoUseCase.executar(processo, dto.clienteId());

        return ResponseEntity.status(HttpStatus.CREATED).body(ProcessoDTO.fromEntity(processoSalvo));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<ProcessoDTO>> listarPorCliente(@PathVariable UUID clienteId) {
        List<Processo> processos = listarProcessosPorClienteUseCase.executar(clienteId);
        List<ProcessoDTO> response = new ArrayList<>();
        for (Processo processo : processos) {
            response.add(ProcessoDTO.fromEntity(processo));
        }

        return ResponseEntity.ok(response);
    }
}