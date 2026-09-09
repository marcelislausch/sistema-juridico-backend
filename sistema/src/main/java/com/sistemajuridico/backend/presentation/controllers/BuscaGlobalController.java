package com.sistemajuridico.backend.presentation.controllers;

import com.sistemajuridico.backend.core.domain.enums.TipoItemBuscaEnum;
import com.sistemajuridico.backend.core.usecases.BuscaGlobalUseCase;
import com.sistemajuridico.backend.presentation.dtos.ItemBuscaDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/busca")
@Tag(name = "Busca Global", description = "Pesquisa unificada em múltiplos domínios (Processos, Clientes, Equipe)")
public class BuscaGlobalController {

    private final BuscaGlobalUseCase buscaGlobalUseCase;

    public BuscaGlobalController(BuscaGlobalUseCase buscaGlobalUseCase) {
        this.buscaGlobalUseCase = buscaGlobalUseCase;
    }

    @GetMapping
    @Operation(summary = "Realiza pesquisa global textual consolidando resultados de processos, clientes e usuários")
    public ResponseEntity<List<ItemBuscaDTO>> buscar(
            @RequestParam String q,
            @RequestParam(required = false) List<TipoItemBuscaEnum> tipos,
            @RequestParam(defaultValue = "10") int limit) {
        List<ItemBuscaDTO> resultados = this.buscaGlobalUseCase.executar(q, tipos, limit);
        return ResponseEntity.ok(resultados);
    }
}
