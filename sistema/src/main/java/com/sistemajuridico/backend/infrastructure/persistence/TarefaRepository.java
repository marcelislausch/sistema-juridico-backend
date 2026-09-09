package com.sistemajuridico.backend.infrastructure.persistence;

import com.sistemajuridico.backend.core.domain.Tarefa;
import com.sistemajuridico.backend.core.domain.enums.TipoTarefaEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TarefaRepository extends JpaRepository<Tarefa, UUID> {
    List<Tarefa> findByUsuarioIdAndConcluidaFalseOrderByDataVencimentoAsc(UUID usuarioId);
    List<Tarefa> findByUsuarioIdOrderByDataVencimentoAsc(UUID usuarioId);
    List<Tarefa> findByUsuarioIdAndDataVencimentoBetween(UUID usuarioId, LocalDate inicio, LocalDate fim);

    @Query(value = "SELECT t.* FROM tb_tarefa t WHERE " +
                   "(CAST(:usuarioId AS uuid) IS NULL OR t.usuario_id = CAST(:usuarioId AS uuid)) AND " +
                   "(CAST(:inicio AS date) IS NULL OR t.data_vencimento >= CAST(:inicio AS date)) AND " +
                   "(CAST(:fim AS date) IS NULL OR t.data_vencimento <= CAST(:fim AS date)) AND " +
                   "(CAST(:concluida AS boolean) IS NULL OR t.concluida = CAST(:concluida AS boolean)) AND " +
                   "(CAST(:tipo AS text) IS NULL OR t.tipo = CAST(:tipo AS text)) AND " +
                   "(CAST(:processoId AS uuid) IS NULL OR t.processo_id = CAST(:processoId AS uuid)) " +
                   "ORDER BY t.data_vencimento ASC",
           nativeQuery = true)
    List<Tarefa> buscarAgenda(@Param("usuarioId") UUID usuarioId,
                              @Param("inicio") LocalDate inicio,
                              @Param("fim") LocalDate fim,
                              @Param("concluida") Boolean concluida,
                              @Param("tipo") String tipo,
                              @Param("processoId") UUID processoId);
}
