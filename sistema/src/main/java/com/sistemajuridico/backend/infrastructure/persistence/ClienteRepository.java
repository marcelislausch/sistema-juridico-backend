package com.sistemajuridico.backend.infrastructure.persistence;

import com.sistemajuridico.backend.core.domain.Cliente;
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
}
