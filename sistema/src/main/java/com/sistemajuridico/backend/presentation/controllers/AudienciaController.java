package com.sistemajuridico.backend.presentation.controllers;

import com.sistemajuridico.backend.core.domain.Audiencia;
import com.sistemajuridico.backend.core.domain.enums.StatusAudienciaEnum;
import com.sistemajuridico.backend.core.usecases.AlterarStatusAudienciaUseCase;
import com.sistemajuridico.backend.core.usecases.CadastrarAudienciaUseCase;
import com.sistemajuridico.backend.core.usecases.ListarAudienciasPorProcessoUseCase;
import com.sistemajuridico.backend.presentation.dtos.AudienciaDTO;
import jakarta.validation.Valid;
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
    private final AlterarStatusAudienciaUseCase alterarStatusAudienciaUseCase;

    public AudienciaController(CadastrarAudienciaUseCase cadastrarAudienciaUseCase,
                               ListarAudienciasPorProcessoUseCase listarAudienciasPorProcessoUseCase,
                               AlterarStatusAudienciaUseCase alterarStatusAudienciaUseCase) {
        this.cadastrarAudienciaUseCase = cadastrarAudienciaUseCase;
        this.listarAudienciasPorProcessoUseCase = listarAudienciasPorProcessoUseCase;
        this.alterarStatusAudienciaUseCase = alterarStatusAudienciaUseCase;
    }

    @PostMapping
    public ResponseEntity<AudienciaDTO> criar(@RequestBody @Valid AudienciaDTO dto) {
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

    @PatchMapping("/{id}/status")
    public ResponseEntity<AudienciaDTO> alterarStatus(@PathVariable UUID id, @RequestParam StatusAudienciaEnum status) {
        Audiencia audienciaAtualizada = alterarStatusAudienciaUseCase.executar(id, status);
        return ResponseEntity.ok(AudienciaDTO.fromEntity(audienciaAtualizada));
    }
}
