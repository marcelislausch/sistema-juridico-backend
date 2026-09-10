package com.sistemajuridico.backend.infrastructure.document;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class PdfExtractionService {

    private static final Logger logger = LoggerFactory.getLogger(PdfExtractionService.class);

    public String extrairTexto(byte[] arquivo) {
        if (arquivo == null || arquivo.length == 0) {
            return "";
        }

        try (PDDocument document = PDDocument.load(arquivo)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String textoExtraido = stripper.getText(document);
            if (textoExtraido == null) {
                return "";
            }
            return textoExtraido.trim();
        } catch (IOException e) {
            logger.error("Falha de E/S ao extrair texto do documento PDF: {}", e.getMessage());
            return "";
        } catch (Exception e) {
            logger.error("Erro inesperado ao processar documento PDF: {}", e.getMessage());
            return "";
        }
    }
}
