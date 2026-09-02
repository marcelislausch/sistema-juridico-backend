package com.sistemajuridico.backend.presentation.controllers;

import com.sistemajuridico.backend.core.domain.Audiencia;
import com.sistemajuridico.backend.core.domain.enums.StatusAudienciaEnum;
import com.sistemajuridico.backend.core.usecases.*;
import com.sistemajuridico.backend.presentation.dtos.AudienciaDTO;
import com.sistemajuridico.backend.presentation.dtos.GerarResumoAudienciaDTO;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/audiencias")
public class AudienciaController {

    private final CadastrarAudienciaUseCase cadastrarAudienciaUseCase;
    private final ListarAudienciasPorProcessoUseCase listarAudienciasPorProcessoUseCase;
    private final AlterarStatusAudienciaUseCase alterarStatusAudienciaUseCase;
    private final ListarAgendaGlobalUseCase listarAgendaGlobalUseCase;
    private final GerarEAnexarResumoAudienciaUseCase gerarEAnexarResumoAudienciaUseCase;
    private final BuscarAudienciaPorIdUseCase buscarAudienciaPorIdUseCase;
    private final AtualizarAudienciaUseCase atualizarAudienciaUseCase;
    private final ExcluirAudienciaUseCase excluirAudienciaUseCase;

    public AudienciaController(CadastrarAudienciaUseCase cadastrarAudienciaUseCase,
                               ListarAudienciasPorProcessoUseCase listarAudienciasPorProcessoUseCase,
                               AlterarStatusAudienciaUseCase alterarStatusAudienciaUseCase,
                               ListarAgendaGlobalUseCase listarAgendaGlobalUseCase,
                               GerarEAnexarResumoAudienciaUseCase gerarEAnexarResumoAudienciaUseCase,
                               BuscarAudienciaPorIdUseCase buscarAudienciaPorIdUseCase,
                               AtualizarAudienciaUseCase atualizarAudienciaUseCase,
                               ExcluirAudienciaUseCase excluirAudienciaUseCase) {
        this.cadastrarAudienciaUseCase = cadastrarAudienciaUseCase;
        this.listarAudienciasPorProcessoUseCase = listarAudienciasPorProcessoUseCase;
        this.alterarStatusAudienciaUseCase = alterarStatusAudienciaUseCase;
        this.listarAgendaGlobalUseCase = listarAgendaGlobalUseCase;
        this.gerarEAnexarResumoAudienciaUseCase = gerarEAnexarResumoAudienciaUseCase;
        this.buscarAudienciaPorIdUseCase = buscarAudienciaPorIdUseCase;
        this.atualizarAudienciaUseCase = atualizarAudienciaUseCase;
        this.excluirAudienciaUseCase = excluirAudienciaUseCase;
    }

    @PostMapping
    public ResponseEntity<AudienciaDTO> criar(@RequestBody @Valid AudienciaDTO dto) {
        Audiencia audiencia = dto.toEntity();
        Audiencia audienciaSalva = cadastrarAudienciaUseCase.executar(audiencia, dto.processoId());
        return ResponseEntity.status(HttpStatus.CREATED).body(AudienciaDTO.fromEntity(audienciaSalva));
    }

    @GetMapping("/agenda")
    public ResponseEntity<List<AudienciaDTO>> listarAgenda(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        List<Audiencia> audiencias = listarAgendaGlobalUseCase.executar(inicio, fim);
        List<AudienciaDTO> response = new ArrayList<>();
        for (Audiencia audiencia : audiencias) {
            response.add(AudienciaDTO.fromEntity(audiencia));
        }
        return ResponseEntity.ok(response);
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

    @GetMapping("/{id}")
    public ResponseEntity<AudienciaDTO> buscarPorId(@PathVariable UUID id) {
        Audiencia audiencia = this.buscarAudienciaPorIdUseCase.executar(id);
        return ResponseEntity.ok(AudienciaDTO.fromEntity(audiencia));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AudienciaDTO> atualizar(@PathVariable UUID id, @RequestBody @Valid AudienciaDTO dto) {
        Audiencia audiencia = dto.toEntity();
        Audiencia audienciaAtualizada = this.atualizarAudienciaUseCase.executar(id, audiencia);
        return ResponseEntity.ok(AudienciaDTO.fromEntity(audienciaAtualizada));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        this.excluirAudienciaUseCase.executar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AudienciaDTO> alterarStatus(@PathVariable UUID id, @RequestParam StatusAudienciaEnum status) {
        Audiencia audienciaAtualizada = alterarStatusAudienciaUseCase.executar(id, status);
        return ResponseEntity.ok(AudienciaDTO.fromEntity(audienciaAtualizada));
    }

    @PostMapping("/{id}/gerar-resumo-ia")
    public ResponseEntity<AudienciaDTO> gerarResumoIa(@PathVariable UUID id, @RequestBody @Valid GerarResumoAudienciaDTO dto) {
        Audiencia audienciaAtualizada = this.gerarEAnexarResumoAudienciaUseCase.executar(id, dto.conteudoPeca());
        return ResponseEntity.ok(AudienciaDTO.fromEntity(audienciaAtualizada));
    }
}
