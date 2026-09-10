package com.sistemajuridico.backend.presentation.controllers;

import com.sistemajuridico.backend.presentation.openapi.EscritorioConfigControllerOpenApi;

import com.sistemajuridico.backend.core.service.EscritorioService;
import com.sistemajuridico.backend.presentation.dtos.EscritorioDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/configuracoes/escritorio")
public class EscritorioConfigController implements EscritorioConfigControllerOpenApi {

    private final EscritorioService escritorioService;

    public EscritorioConfigController(EscritorioService escritorioService) {
        this.escritorioService = escritorioService;
    }

    @Override
    @GetMapping
    public ResponseEntity<EscritorioDTO> obterConfiguracao() {
        EscritorioDTO dto = this.escritorioService.obterDadosEscritorio();
        return ResponseEntity.ok(dto);
    }

    @Override
    @PutMapping
    public ResponseEntity<EscritorioDTO> atualizarConfiguracao(@RequestBody @Valid EscritorioDTO dto) {
        EscritorioDTO atualizado = this.escritorioService.atualizarDadosEscritorio(dto);
        return ResponseEntity.ok(atualizado);
    }
}
