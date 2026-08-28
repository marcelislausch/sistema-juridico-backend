package com.juridia.sistema.infrastructure.persistence;

import com.juridia.sistema.core.domain.Processo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProcessoRepository extends JpaRepository<Processo, UUID> {

}