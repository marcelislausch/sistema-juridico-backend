package com.sistemajuridico.backend.infrastructure.persistence;

import com.sistemajuridico.backend.core.domain.Audiencia;
import com.sistemajuridico.backend.core.domain.enums.StatusAudienciaEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AudienciaRepository extends JpaRepository<Audiencia, UUID> {
    List<Audiencia> findByProcessoId(UUID processoId);
    List<Audiencia> findByDataHoraBetween(LocalDateTime inicio, LocalDateTime fim);
    List<Audiencia> findByDataHoraBetweenOrderByDataHoraAsc(LocalDateTime inicio, LocalDateTime fim);

    @Query(value = "SELECT a.* FROM tb_audiencia a " +
                   "LEFT JOIN tb_processo p ON p.id = a.processo_id WHERE " +
                   "(CAST(:inicio AS timestamp) IS NULL OR a.data_hora >= CAST(:inicio AS timestamp)) AND " +
                   "(CAST(:fim AS timestamp) IS NULL OR a.data_hora <= CAST(:fim AS timestamp)) AND " +
                   "(CAST(:status AS text) IS NULL OR a.status = CAST(:status AS text)) AND " +
                   "(CAST(:processoId AS uuid) IS NULL OR a.processo_id = CAST(:processoId AS uuid)) AND " +
                   "(CAST(:responsavelId AS uuid) IS NULL OR p.usuario_id = CAST(:responsavelId AS uuid)) " +
                   "ORDER BY a.data_hora ASC",
           nativeQuery = true)
    List<Audiencia> buscarAgenda(@Param("inicio") LocalDateTime inicio,
                                @Param("fim") LocalDateTime fim,
                                @Param("status") String status,
                                @Param("processoId") UUID processoId,
                                @Param("responsavelId") UUID responsavelId);
}
