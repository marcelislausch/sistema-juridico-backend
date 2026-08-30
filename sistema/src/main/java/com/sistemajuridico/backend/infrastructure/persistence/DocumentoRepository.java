package com.sistemajuridico.backend.infrastructure.persistence;

import com.sistemajuridico.backend.core.domain.Documento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DocumentoRepository extends JpaRepository<Documento, UUID> {

}
