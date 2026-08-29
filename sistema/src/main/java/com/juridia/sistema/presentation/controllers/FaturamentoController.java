package com.juridia.sistema.presentation.controllers;

import com.juridia.sistema.core.domain.Faturamento;
import com.juridia.sistema.core.usecases.CadastrarFaturamentoUseCase;
import com.juridia.sistema.presentation.dtos.FaturamentoDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/faturamentos")
public class FaturamentoController {

    private final CadastrarFaturamentoUseCase cadastrarFaturamentoUseCase;

    public FaturamentoController(CadastrarFaturamentoUseCase cadastrarFaturamentoUseCase) {
        this.cadastrarFaturamentoUseCase = cadastrarFaturamentoUseCase;
    }

    @PostMapping
    public ResponseEntity<FaturamentoDTO> criar(@RequestBody FaturamentoDTO dto) {
        Faturamento faturamento = dto.toEntity();
        Faturamento faturamentoSalvo = cadastrarFaturamentoUseCase.executar(faturamento, dto.processoId());
        return ResponseEntity.status(HttpStatus.CREATED).body(FaturamentoDTO.fromEntity(faturamentoSalvo));
    }
}
