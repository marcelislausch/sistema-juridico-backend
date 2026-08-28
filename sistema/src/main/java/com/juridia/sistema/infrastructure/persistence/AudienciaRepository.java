package com.juridia.sistema.infrastructure.persistence;

import com.juridia.sistema.core.domain.Audiencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AudienciaRepository extends JpaRepository<Audiencia, Long> {

}