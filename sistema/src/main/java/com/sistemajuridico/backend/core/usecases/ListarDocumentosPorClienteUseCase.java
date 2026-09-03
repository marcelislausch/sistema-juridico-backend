package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Documento;
import com.sistemajuridico.backend.core.domain.exceptions.RegraNegocioException;
import com.sistemajuridico.backend.infrastructure.persistence.DocumentoRepository;
import com.sistemajuridico.backend.presentation.dtos.DocumentoDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ListarDocumentosPorClienteUseCase {

    private final DocumentoRepository documentoRepository;

    public ListarDocumentosPorClienteUseCase(DocumentoRepository documentoRepository) {
        this.documentoRepository = documentoRepository;
    }

    public List<DocumentoDTO> executar(UUID clienteId) {
        if (clienteId == null) {
            throw new RegraNegocioException("O ID do cliente é obrigatório para listar os documentos.");
        }

        List<Documento> documentos = this.documentoRepository.findByClienteId(clienteId);
        List<DocumentoDTO> resultado = new ArrayList<>();
        for (Documento documento : documentos) {
            resultado.add(DocumentoDTO.fromEntity(documento));
        }

        return resultado;
    }
}
