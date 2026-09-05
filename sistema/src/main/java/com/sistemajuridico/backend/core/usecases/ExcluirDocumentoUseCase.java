package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Documento;
import com.sistemajuridico.backend.core.domain.exceptions.RecursoNaoEncontradoException;
import com.sistemajuridico.backend.core.domain.exceptions.RegraNegocioException;
import com.sistemajuridico.backend.infrastructure.persistence.DocumentoRepository;
import com.sistemajuridico.backend.infrastructure.storage.StorageService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

@Service
public class ExcluirDocumentoUseCase {

    private final DocumentoRepository documentoRepository;
    private final StorageService storageService;

    public ExcluirDocumentoUseCase(DocumentoRepository documentoRepository, StorageService storageService) {
        this.documentoRepository = documentoRepository;
        this.storageService = storageService;
    }

    public void executar(UUID id) {
        if (id == null) {
            throw new RegraNegocioException("O ID do documento é obrigatório para exclusão.");
        }

        Optional<Documento> optDocumento = this.documentoRepository.findById(id);
        if (optDocumento.isEmpty()) {
            throw new RecursoNaoEncontradoException("Documento não encontrado no sistema!");
        }

        Documento documento = optDocumento.get();
        if (documento.getCaminhoStorage() != null && !documento.getCaminhoStorage().trim().isEmpty()) {
            String caminhoOuId = documento.getCaminhoStorage().trim();
            Path caminhoArquivo = Paths.get(caminhoOuId);
            try {
                if (Files.exists(caminhoArquivo)) {
                    Files.delete(caminhoArquivo);
                } else {
                    this.storageService.excluirArquivo(caminhoOuId);
                }
            } catch (Exception e) {
                // Trata silenciosamente caso ocorra falha ao remover arquivo do disco ou storage
            }
        }

        this.documentoRepository.delete(documento);
    }
}
