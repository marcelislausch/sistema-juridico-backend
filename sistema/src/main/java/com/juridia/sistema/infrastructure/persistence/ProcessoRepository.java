package com.juridia.sistema.infrastructure.persistence;

import com.juridia.sistema.core.domain.Processo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessoRepository extends JpaRepository<Processo, Long> {

}