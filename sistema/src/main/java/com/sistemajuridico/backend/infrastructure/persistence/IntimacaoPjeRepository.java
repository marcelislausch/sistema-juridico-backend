package com.sistemajuridico.backend.infrastructure.persistence;

import com.sistemajuridico.backend.core.domain.IntimacaoPje;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IntimacaoPjeRepository extends JpaRepository<IntimacaoPje, UUID> {

    Optional<IntimacaoPje> findByComunicacaoId(Long comunicacaoId);

    boolean existsByComunicacaoId(Long comunicacaoId);

    List<IntimacaoPje> findByProcessoIdOrderByDataDisponibilizacaoDesc(UUID processoId);

    List<IntimacaoPje> findByAdvogadoIdOrderByDataDisponibilizacaoDesc(UUID advogadoId);

    @Query(value = "SELECT i.* FROM tb_intimacao_pje i WHERE " +
                   "(CAST(:advogadoId AS uuid) IS NULL OR i.usuario_id = CAST(:advogadoId AS uuid)) AND " +
                   "(CAST(:processoId AS uuid) IS NULL OR i.processo_id = CAST(:processoId AS uuid)) " +
                   "ORDER BY i.data_disponibilizacao DESC",
           countQuery = "SELECT count(i.id) FROM tb_intimacao_pje i WHERE " +
                        "(CAST(:advogadoId AS uuid) IS NULL OR i.usuario_id = CAST(:advogadoId AS uuid)) AND " +
                        "(CAST(:processoId AS uuid) IS NULL OR i.processo_id = CAST(:processoId AS uuid))",
           nativeQuery = true)
    Page<IntimacaoPje> listarPaginado(@Param("advogadoId") UUID advogadoId,
                                      @Param("processoId") UUID processoId,
                                      Pageable pageable);
}
