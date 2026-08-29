package com.juridia.sistema.presentation.controllers;

import com.juridia.sistema.core.domain.Audiencia;
import com.juridia.sistema.core.usecases.CadastrarAudienciaUseCase;
import com.juridia.sistema.core.usecases.ListarAudienciasPorProcessoUseCase;
import com.juridia.sistema.presentation.dtos.AudienciaDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/audiencias")
public class AudienciaController {

    private final CadastrarAudienciaUseCase cadastrarAudienciaUseCase;
    private final ListarAudienciasPorProcessoUseCase listarAudienciasPorProcessoUseCase;

    public AudienciaController(CadastrarAudienciaUseCase cadastrarAudienciaUseCase,
                               ListarAudienciasPorProcessoUseCase listarAudienciasPorProcessoUseCase) {
        this.cadastrarAudienciaUseCase = cadastrarAudienciaUseCase;
        this.listarAudienciasPorProcessoUseCase = listarAudienciasPorProcessoUseCase;
    }

    @PostMapping
    public ResponseEntity<AudienciaDTO> criar(@RequestBody AudienciaDTO dto) {
        Audiencia audiencia = dto.toEntity();
        Audiencia audienciaSalva = cadastrarAudienciaUseCase.executar(audiencia, dto.processoId());
        return ResponseEntity.status(HttpStatus.CREATED).body(AudienciaDTO.fromEntity(audienciaSalva));
    }

    @GetMapping("/processo/{processoId}")
    public ResponseEntity<List<AudienciaDTO>> listarPorProcesso(@PathVariable UUID processoId) {
        List<Audiencia> audiencias = listarAudienciasPorProcessoUseCase.executar(processoId);
        List<AudienciaDTO> response = new ArrayList<>();
        for (Audiencia audiencia : audiencias) {
            response.add(AudienciaDTO.fromEntity(audiencia));
        }

        return ResponseEntity.ok(response);
    }
}
