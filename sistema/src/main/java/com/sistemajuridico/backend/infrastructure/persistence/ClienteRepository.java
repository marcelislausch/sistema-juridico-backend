package com.sistemajuridico.backend.infrastructure.persistence;

import com.sistemajuridico.backend.core.domain.Cliente;
import com.sistemajuridico.backend.core.domain.enums.TipoClienteEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, UUID> {

    @Query("SELECT c FROM Cliente c WHERE " +
           "(LOWER(c.nome) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
           "c.cpfCnpj LIKE CONCAT('%', :termo, '%') OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :termo, '%')))")
    Page<Cliente> buscarPorTermo(@Param("termo") String termo, Pageable pageable);

    @Query(value = "SELECT c.* FROM tb_cliente c WHERE " +
                   "(CAST(:tipo AS text) IS NULL OR c.tipo = CAST(:tipo AS text)) AND " +
                   "(CAST(:termo AS text) IS NULL OR (" +
                   "lower(c.nome) LIKE lower(concat('%', CAST(:termo AS text), '%')) OR " +
                   "lower(c.cpf_cnpj) LIKE lower(concat('%', CAST(:termo AS text), '%')) OR " +
                   "lower(c.email) LIKE lower(concat('%', CAST(:termo AS text), '%'))))",
           countQuery = "SELECT count(c.id) FROM tb_cliente c WHERE " +
                        "(CAST(:tipo AS text) IS NULL OR c.tipo = CAST(:tipo AS text)) AND " +
                        "(CAST(:termo AS text) IS NULL OR (" +
                        "lower(c.nome) LIKE lower(concat('%', CAST(:termo AS text), '%')) OR " +
                        "lower(c.cpf_cnpj) LIKE lower(concat('%', CAST(:termo AS text), '%')) OR " +
                        "lower(c.email) LIKE lower(concat('%', CAST(:termo AS text), '%'))))",
           nativeQuery = true)
    Page<Cliente> buscarComFiltros(@Param("tipo") String tipo, @Param("termo") String termo, Pageable pageable);
}
