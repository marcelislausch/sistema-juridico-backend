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
}
