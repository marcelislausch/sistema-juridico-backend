package com.sistemajuridico.backend.infrastructure.persistence;

import com.sistemajuridico.backend.core.domain.Faturamento;
import com.sistemajuridico.backend.core.domain.enums.NaturezaFaturamentoEnum;
import com.sistemajuridico.backend.core.domain.enums.StatusFaturamentoEnum;
import com.sistemajuridico.backend.core.domain.enums.TipoFaturamentoEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface FaturamentoRepository extends JpaRepository<Faturamento, UUID> {
    List<Faturamento> findByProcessoId(UUID processoId);
    List<Faturamento> findByProcessoIdAndStatus(UUID processoId, StatusFaturamentoEnum status);
    List<Faturamento> findByStatusAndDataVencimentoAndNatureza(StatusFaturamentoEnum status, LocalDate dataVencimento, NaturezaFaturamentoEnum natureza);
    List<Faturamento> findByStatusAndDataVencimento(StatusFaturamentoEnum status, LocalDate dataVencimento);
    List<Faturamento> findByStatusAndNaturezaOrderByDataVencimentoAsc(StatusFaturamentoEnum status, NaturezaFaturamentoEnum natureza);

    Page<Faturamento> findByStatusAndNatureza(StatusFaturamentoEnum status, NaturezaFaturamentoEnum natureza, Pageable pageable);
    Page<Faturamento> findByStatus(StatusFaturamentoEnum status, Pageable pageable);
    Page<Faturamento> findByNatureza(NaturezaFaturamentoEnum natureza, Pageable pageable);

    @Query("SELECT SUM(f.valor) FROM Faturamento f WHERE f.natureza = :natureza AND f.status = :status")
    BigDecimal somarPorNaturezaEStatus(@Param("natureza") NaturezaFaturamentoEnum natureza, @Param("status") StatusFaturamentoEnum status);

    @Query("SELECT SUM(f.valor) FROM Faturamento f WHERE f.natureza = :natureza AND f.status = :status AND f.dataPagamento BETWEEN :inicio AND :fim")
    BigDecimal somarPorNaturezaEStatusEDataPagamentoBetween(@Param("natureza") NaturezaFaturamentoEnum natureza,
                                                          @Param("status") StatusFaturamentoEnum status,
                                                          @Param("inicio") LocalDate inicio,
                                                          @Param("fim") LocalDate fim);

    @Query(value = "SELECT f.* FROM tb_faturamento f " +
                   "LEFT JOIN tb_processo p ON p.id = f.processo_id WHERE " +
                   "(CAST(:status AS text) IS NULL OR f.status = CAST(:status AS text)) AND " +
                   "(CAST(:natureza AS text) IS NULL OR f.natureza = CAST(:natureza AS text)) AND " +
                   "(CAST(:tipo AS text) IS NULL OR f.tipo = CAST(:tipo AS text)) AND " +
                   "(CAST(:vencimentoDe AS date) IS NULL OR f.data_vencimento >= CAST(:vencimentoDe AS date)) AND " +
                   "(CAST(:vencimentoAte AS date) IS NULL OR f.data_vencimento <= CAST(:vencimentoAte AS date)) AND " +
                   "(CAST(:processoId AS uuid) IS NULL OR f.processo_id = CAST(:processoId AS uuid)) AND " +
                   "(CAST(:q AS text) IS NULL OR (" +
                   "  lower(f.descricao) LIKE lower(concat('%', CAST(:q AS text), '%')) OR " +
                   "  lower(p.numero_cnj) LIKE lower(concat('%', CAST(:q AS text), '%'))))",
           countQuery = "SELECT count(f.id) FROM tb_faturamento f " +
                        "LEFT JOIN tb_processo p ON p.id = f.processo_id WHERE " +
                        "(CAST(:status AS text) IS NULL OR f.status = CAST(:status AS text)) AND " +
                        "(CAST(:natureza AS text) IS NULL OR f.natureza = CAST(:natureza AS text)) AND " +
                        "(CAST(:tipo AS text) IS NULL OR f.tipo = CAST(:tipo AS text)) AND " +
                        "(CAST(:vencimentoDe AS date) IS NULL OR f.data_vencimento >= CAST(:vencimentoDe AS date)) AND " +
                        "(CAST(:vencimentoAte AS date) IS NULL OR f.data_vencimento <= CAST(:vencimentoAte AS date)) AND " +
                        "(CAST(:processoId AS uuid) IS NULL OR f.processo_id = CAST(:processoId AS uuid)) AND " +
                        "(CAST(:q AS text) IS NULL OR (" +
                        "  lower(f.descricao) LIKE lower(concat('%', CAST(:q AS text), '%')) OR " +
                        "  lower(p.numero_cnj) LIKE lower(concat('%', CAST(:q AS text), '%'))))",
           nativeQuery = true)
    Page<Faturamento> buscarComFiltros(@Param("q") String q,
                                       @Param("status") String status,
                                       @Param("natureza") String natureza,
                                       @Param("tipo") String tipo,
                                       @Param("vencimentoDe") LocalDate vencimentoDe,
                                       @Param("vencimentoAte") LocalDate vencimentoAte,
                                       @Param("processoId") UUID processoId,
                                       Pageable pageable);
}
