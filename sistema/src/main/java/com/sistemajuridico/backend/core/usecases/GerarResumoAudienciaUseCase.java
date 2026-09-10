package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Documento;
import com.sistemajuridico.backend.core.domain.dto.ResumoAudienciaEstruturadoDTO;
import com.sistemajuridico.backend.core.domain.exceptions.RegraNegocioException;
import com.sistemajuridico.backend.infrastructure.ai.ResumoAIService;
import com.sistemajuridico.backend.infrastructure.ai.TextoChunkingService;
import com.sistemajuridico.backend.infrastructure.document.PdfExtractionService;
import com.sistemajuridico.backend.infrastructure.persistence.DocumentoRepository;
import com.sistemajuridico.backend.infrastructure.storage.GoogleDriveStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class GerarResumoAudienciaUseCase {

    private static final Logger logger = LoggerFactory.getLogger(GerarResumoAudienciaUseCase.class);

    private final DocumentoRepository documentoRepository;
    private final GoogleDriveStorageService googleDriveStorageService;
    private final PdfExtractionService pdfExtractionService;
    private final TextoChunkingService textoChunkingService;
    private final ResumoAIService resumoAIService;

    public GerarResumoAudienciaUseCase(DocumentoRepository documentoRepository,
                                       GoogleDriveStorageService googleDriveStorageService,
                                       PdfExtractionService pdfExtractionService,
                                       TextoChunkingService textoChunkingService,
                                       ResumoAIService resumoAIService) {
        this.documentoRepository = documentoRepository;
        this.googleDriveStorageService = googleDriveStorageService;
        this.pdfExtractionService = pdfExtractionService;
        this.textoChunkingService = textoChunkingService;
        this.resumoAIService = resumoAIService;
    }

    public ResumoAudienciaEstruturadoDTO executar(List<String> documentosIds) {
        if (documentosIds == null || documentosIds.isEmpty()) {
            throw new RegraNegocioException("A lista de IDs de documentos é obrigatória para gerar o resumo.");
        }

        StringBuilder textoConsolidado = new StringBuilder();

        for (int i = 0; i < documentosIds.size(); i++) {
            String docId = documentosIds.get(i);
            if (docId == null || docId.trim().isEmpty()) {
                continue;
            }

            String fileIdParaDownload = docId.trim();

            try {
                UUID uuid = UUID.fromString(fileIdParaDownload);
                Optional<Documento> optDoc = this.documentoRepository.findById(uuid);
                if (optDoc.isPresent()) {
                    Documento doc = optDoc.get();
                    if (doc.getCaminhoStorage() != null && !doc.getCaminhoStorage().trim().isEmpty()) {
                        fileIdParaDownload = doc.getCaminhoStorage().trim();
                    }
                }
            } catch (IllegalArgumentException e) {
                // docId não é UUID, assume ID direto do Google Drive
            }

            try {
                byte[] bytesArquivo = this.googleDriveStorageService.downloadArquivo(fileIdParaDownload);
                if (bytesArquivo != null && bytesArquivo.length > 0) {
                    String textoExtraido = this.pdfExtractionService.extrairTexto(bytesArquivo);
                    if (textoExtraido != null && !textoExtraido.trim().isEmpty()) {
                        if (textoConsolidado.length() > 0) {
                            textoConsolidado.append("\n\n--- DOCUMENTO ").append(i + 1).append(" ---\n\n");
                        }
                        textoConsolidado.append(textoExtraido.trim());
                    }
                }
            } catch (Exception e) {
                logger.error("Erro ao baixar ou extrair texto do documento '{}': {}", fileIdParaDownload, e.getMessage());
            }
        }

        if (textoConsolidado.length() == 0) {
            throw new RegraNegocioException("Nenhum conteúdo textual pôde ser extraído dos documentos informados para gerar o resumo.");
        }

        String textoConsolidadoStr = textoConsolidado.toString();
        List<String> chunks = this.textoChunkingService.dividirEmChunks(textoConsolidadoStr);

        String textoFinalParaResumo;

        if (chunks.size() <= 1) {
            textoFinalParaResumo = textoConsolidadoStr;
        } else {
            logger.info("Processo extenso detectado ({} caracteres). Fatiando em {} chunks para consolidação progressiva.",
                    textoConsolidadoStr.length(), chunks.size());

            StringBuilder sinteseConsolidada = new StringBuilder();

            for (int i = 0; i < chunks.size(); i++) {
                String chunk = chunks.get(i);
                String sinteseChunk = this.resumoAIService.resumirChunk(chunk, i + 1, chunks.size());
                if (sinteseChunk != null && !sinteseChunk.trim().isEmpty()) {
                    if (sinteseConsolidada.length() > 0) {
                        sinteseConsolidada.append("\n\n");
                    }
                    sinteseConsolidada.append("=== SÍNTESE DO BLOCO ").append(i + 1).append(" DE ").append(chunks.size()).append(" ===\n");
                    sinteseConsolidada.append(sinteseChunk.trim());
                }
            }

            if (sinteseConsolidada.length() > 0) {
                textoFinalParaResumo = sinteseConsolidada.toString();
            } else {
                textoFinalParaResumo = textoConsolidadoStr;
            }
        }

        return this.resumoAIService.resumirPecas(textoFinalParaResumo);
    }
}


