package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Documento;
import com.sistemajuridico.backend.core.domain.exceptions.RecursoNaoEncontradoException;
import com.sistemajuridico.backend.core.domain.exceptions.RegraNegocioException;
import com.sistemajuridico.backend.infrastructure.persistence.DocumentoRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class BuscarDocumentoPorIdUseCase {

    private final DocumentoRepository documentoRepository;

    public BuscarDocumentoPorIdUseCase(DocumentoRepository documentoRepository) {
        this.documentoRepository = documentoRepository;
    }

    public Documento executar(UUID id) {
        if (id == null) {
            throw new RegraNegocioException("O ID do documento é obrigatório para a consulta.");
        }

        Optional<Documento> optDocumento = this.documentoRepository.findById(id);
        if (optDocumento.isEmpty()) {
            throw new RecursoNaoEncontradoException("Documento não encontrado no sistema!");
        }

        return optDocumento.get();
    }
}
