package com.juridia.sistema.presentation.controllers;

import com.juridia.sistema.core.domain.Audiencia;
import com.juridia.sistema.core.usecases.CadastrarAudienciaUseCase;
import com.juridia.sistema.presentation.dtos.AudienciaDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audiencias")
public class AudienciaController {

    private final CadastrarAudienciaUseCase cadastrarAudienciaUseCase;

    public AudienciaController(CadastrarAudienciaUseCase cadastrarAudienciaUseCase) {
        this.cadastrarAudienciaUseCase = cadastrarAudienciaUseCase;
    }

    @PostMapping
    public ResponseEntity<AudienciaDTO> criar(@RequestBody AudienciaDTO dto) {
        Audiencia audiencia = dto.toEntity();
        Audiencia audienciaSalva = cadastrarAudienciaUseCase.executar(audiencia, dto.processoId());
        return ResponseEntity.status(HttpStatus.CREATED).body(AudienciaDTO.fromEntity(audienciaSalva));
    }
}
