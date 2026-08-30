package com.sistemajuridico.backend.presentation.controllers;

import com.sistemajuridico.backend.core.domain.Documento;
import com.sistemajuridico.backend.core.domain.exceptions.RegraNegocioException;
import com.sistemajuridico.backend.core.usecases.UploadDocumentoUseCase;
import com.sistemajuridico.backend.presentation.dtos.DocumentoDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/documentos")
public class DocumentoController {

    private final UploadDocumentoUseCase uploadDocumentoUseCase;

    public DocumentoController(UploadDocumentoUseCase uploadDocumentoUseCase) {
        this.uploadDocumentoUseCase = uploadDocumentoUseCase;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentoDTO> upload(
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam("titulo") String titulo,
            @RequestParam(value = "clienteId", required = false) UUID clienteId,
            @RequestParam(value = "processoId", required = false) UUID processoId) {

        if (arquivo == null || arquivo.isEmpty()) {
            throw new RegraNegocioException("Arquivo não informado ou vazio!");
        }

        byte[] bytes;
        try {
            bytes = arquivo.getBytes();
        } catch (IOException e) {
            throw new RegraNegocioException("Falha ao ler os bytes do arquivo enviado.");
        }

        Documento documentoSalvo = this.uploadDocumentoUseCase.executar(
                bytes,
                arquivo.getOriginalFilename(),
                titulo,
                clienteId,
                processoId
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(DocumentoDTO.fromEntity(documentoSalvo));
    }
}
