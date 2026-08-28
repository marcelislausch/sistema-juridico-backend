package com.juridia.sistema.presentation.controllers;

import com.juridia.sistema.core.domain.Processo;
import com.juridia.sistema.core.usecases.CadastrarProcessoUseCase;
import com.juridia.sistema.presentation.dtos.ProcessoDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/processos")
public class ProcessoController {

    private final CadastrarProcessoUseCase cadastrarProcessoUseCase;

    public ProcessoController(CadastrarProcessoUseCase cadastrarProcessoUseCase) {
        this.cadastrarProcessoUseCase = cadastrarProcessoUseCase;
    }

    @PostMapping
    public ResponseEntity<ProcessoDTO> criar(@RequestBody ProcessoDTO dto) {
        Processo processo = dto.toEntity();
        //Manda para a regra de negócio (passando o clienteId separado para o Use Case buscar no banco)
        Processo processoSalvo = cadastrarProcessoUseCase.executar(processo, dto.clienteId());

        return ResponseEntity.status(HttpStatus.CREATED).body(ProcessoDTO.fromEntity(processoSalvo));
    }
}