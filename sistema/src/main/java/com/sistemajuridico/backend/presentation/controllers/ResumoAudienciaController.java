package com.sistemajuridico.backend.presentation.controllers;

import com.sistemajuridico.backend.presentation.openapi.ResumoAudienciaControllerOpenApi;

import com.sistemajuridico.backend.core.domain.dto.ResumoAudienciaEstruturadoDTO;
import com.sistemajuridico.backend.core.usecases.GerarResumoAudienciaUseCase;
import com.sistemajuridico.backend.presentation.dtos.ResumoPecaDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ia/resumos")
public class ResumoAudienciaController implements ResumoAudienciaControllerOpenApi {

    private final GerarResumoAudienciaUseCase gerarResumoAudienciaUseCase;

    public ResumoAudienciaController(GerarResumoAudienciaUseCase gerarResumoAudienciaUseCase) {
        this.gerarResumoAudienciaUseCase = gerarResumoAudienciaUseCase;
    }

    @Override
    @PostMapping("/audiencia")
    public ResponseEntity<ResumoAudienciaEstruturadoDTO> resumirParaAudiencia(@RequestBody @Valid ResumoPecaDTO dto) {
        ResumoAudienciaEstruturadoDTO resumo = this.gerarResumoAudienciaUseCase.executar(dto.documentosIds());
        return ResponseEntity.ok(resumo);
    }
}

