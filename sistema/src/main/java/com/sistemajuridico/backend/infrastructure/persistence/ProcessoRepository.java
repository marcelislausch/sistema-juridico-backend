package com.sistemajuridico.backend.infrastructure.persistence;

import com.sistemajuridico.backend.core.domain.Processo;
import com.sistemajuridico.backend.core.domain.enums.FaseProcessualEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProcessoRepository extends JpaRepository<Processo, UUID> {
    Page<Processo> findByClienteId(UUID clienteId, Pageable pageable);
    int countByAdvogadoIdAndFaseAtualNot(UUID advogadoId, FaseProcessualEnum faseAtual);

    Page<Processo> findByFaseAtual(FaseProcessualEnum faseAtual, Pageable pageable);
    Page<Processo> findByFaseAtualNot(FaseProcessualEnum faseAtual, Pageable pageable);

    @Query("SELECT p FROM Processo p WHERE " +
           "(LOWER(p.numeroCnj) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
           "LOWER(p.assunto) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
           "LOWER(p.cliente.nome) LIKE LOWER(CONCAT('%', :termo, '%')))")
    Page<Processo> buscarPorTermo(@Param("termo") String termo, Pageable pageable);

    @Query("SELECT p FROM Processo p WHERE p.faseAtual = :faseAtual AND " +
           "(LOWER(p.numeroCnj) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
           "LOWER(p.assunto) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
           "LOWER(p.cliente.nome) LIKE LOWER(CONCAT('%', :termo, '%')))")
    Page<Processo> buscarPorFaseETermo(@Param("faseAtual") FaseProcessualEnum faseAtual, @Param("termo") String termo, Pageable pageable);

    @Query("SELECT p FROM Processo p WHERE p.faseAtual != :faseAtual AND " +
           "(LOWER(p.numeroCnj) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
           "LOWER(p.assunto) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
           "LOWER(p.cliente.nome) LIKE LOWER(CONCAT('%', :termo, '%')))")
    Page<Processo> buscarPorFaseDiferenteETermo(@Param("faseAtual") FaseProcessualEnum faseAtual, @Param("termo") String termo, Pageable pageable);

    @Query(value = "SELECT p.* FROM tb_processo p " +
                   "JOIN tb_cliente c ON c.id = p.cliente_id WHERE " +
                   "((CAST(:fase AS text) IS NOT NULL AND p.fase_atual = CAST(:fase AS text)) OR " +
                   " (CAST(:fase AS text) IS NULL AND (" +
                   "   CAST(:arquivado AS boolean) IS NULL OR " +
                   "   (CAST(:arquivado AS boolean) IS TRUE AND p.fase_atual = 'ARQUIVADO') OR " +
                   "   (CAST(:arquivado AS boolean) IS FALSE AND p.fase_atual != 'ARQUIVADO')))) AND " +
                   "(CAST(:clienteId AS uuid) IS NULL OR p.cliente_id = CAST(:clienteId AS uuid)) AND " +
                   "(CAST(:advogadoId AS uuid) IS NULL OR p.usuario_id = CAST(:advogadoId AS uuid)) AND " +
                   "(CAST(:termo AS text) IS NULL OR (" +
                   "  lower(p.numero_cnj) LIKE lower(concat('%', CAST(:termo AS text), '%')) OR " +
                   "  lower(p.assunto) LIKE lower(concat('%', CAST(:termo AS text), '%')) OR " +
                   "  lower(c.nome) LIKE lower(concat('%', CAST(:termo AS text), '%'))))",
           countQuery = "SELECT count(p.id) FROM tb_processo p " +
                        "JOIN tb_cliente c ON c.id = p.cliente_id WHERE " +
                        "((CAST(:fase AS text) IS NOT NULL AND p.fase_atual = CAST(:fase AS text)) OR " +
                        " (CAST(:fase AS text) IS NULL AND (" +
                        "   CAST(:arquivado AS boolean) IS NULL OR " +
                        "   (CAST(:arquivado AS boolean) IS TRUE AND p.fase_atual = 'ARQUIVADO') OR " +
                        "   (CAST(:arquivado AS boolean) IS FALSE AND p.fase_atual != 'ARQUIVADO')))) AND " +
                        "(CAST(:clienteId AS uuid) IS NULL OR p.cliente_id = CAST(:clienteId AS uuid)) AND " +
                        "(CAST(:advogadoId AS uuid) IS NULL OR p.usuario_id = CAST(:advogadoId AS uuid)) AND " +
                        "(CAST(:termo AS text) IS NULL OR (" +
                        "  lower(p.numero_cnj) LIKE lower(concat('%', CAST(:termo AS text), '%')) OR " +
                        "  lower(p.assunto) LIKE lower(concat('%', CAST(:termo AS text), '%')) OR " +
                        "  lower(c.nome) LIKE lower(concat('%', CAST(:termo AS text), '%'))))",
           nativeQuery = true)
    Page<Processo> buscarComFiltros(@Param("termo") String termo,
                                    @Param("fase") String fase,
                                    @Param("arquivado") Boolean arquivado,
                                    @Param("clienteId") UUID clienteId,
                                    @Param("advogadoId") UUID advogadoId,
                                    Pageable pageable);

    @Query(value = "SELECT * FROM tb_processo WHERE numero_cnj = :numeroCnj LIMIT 1", nativeQuery = true)
    java.util.Optional<Processo> findByNumeroCnjExato(@Param("numeroCnj") String numeroCnj);

    @Query(value = "SELECT * FROM tb_processo WHERE regexp_replace(numero_cnj, '\\D', '', 'g') = :numeroLimpo LIMIT 1", nativeQuery = true)
    java.util.Optional<Processo> findByNumeroCnjLimpo(@Param("numeroLimpo") String numeroLimpo);
}

