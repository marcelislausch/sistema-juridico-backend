package com.juridia.sistema.infrastructure.persistence;

import com.juridia.sistema.core.domain.PrazoProcessual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrazoProcessualRepository extends JpaRepository<PrazoProcessual, Long> {

}