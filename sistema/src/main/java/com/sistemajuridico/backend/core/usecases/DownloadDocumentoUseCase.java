package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Documento;
import com.sistemajuridico.backend.core.domain.exceptions.RecursoNaoEncontradoException;
import com.sistemajuridico.backend.core.domain.exceptions.RegraNegocioException;
import com.sistemajuridico.backend.infrastructure.persistence.DocumentoRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

@Service
public class DownloadDocumentoUseCase {

    private final DocumentoRepository documentoRepository;

    public DownloadDocumentoUseCase(DocumentoRepository documentoRepository) {
        this.documentoRepository = documentoRepository;
    }

    public byte[] executar(UUID id) {
        if (id == null) {
            throw new RegraNegocioException("O ID do documento é obrigatório para download.");
        }

        Optional<Documento> optDocumento = this.documentoRepository.findById(id);
        if (optDocumento.isEmpty()) {
            throw new RecursoNaoEncontradoException("Documento não encontrado no sistema!");
        }

        Documento documento = optDocumento.get();
        if (documento.getCaminhoStorage() == null || documento.getCaminhoStorage().trim().isEmpty()) {
            throw new RegraNegocioException("Caminho do arquivo não informado no registro do documento.");
        }

        Path caminhoArquivo = Paths.get(documento.getCaminhoStorage());
        if (!Files.exists(caminhoArquivo)) {
            throw new RecursoNaoEncontradoException("Arquivo físico não encontrado no armazenamento.");
        }

        try {
            return Files.readAllBytes(caminhoArquivo);
        } catch (IOException e) {
            throw new RegraNegocioException("Falha ao ler os bytes do arquivo físico no disco: " + e.getMessage());
        }
    }
}
