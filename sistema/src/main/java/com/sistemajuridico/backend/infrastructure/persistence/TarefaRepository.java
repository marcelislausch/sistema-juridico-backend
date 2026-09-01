package com.sistemajuridico.backend.infrastructure.persistence;

import com.sistemajuridico.backend.core.domain.Tarefa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TarefaRepository extends JpaRepository<Tarefa, UUID> {
    List<Tarefa> findByUsuarioIdAndConcluidaFalseOrderByDataVencimentoAsc(UUID usuarioId);
    List<Tarefa> findByUsuarioIdOrderByDataVencimentoAsc(UUID usuarioId);
    List<Tarefa> findByUsuarioIdAndDataVencimentoBetween(UUID usuarioId, LocalDate inicio, LocalDate fim);
}
