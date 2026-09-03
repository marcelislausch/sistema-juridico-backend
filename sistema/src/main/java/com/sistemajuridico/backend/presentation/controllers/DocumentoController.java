package com.sistemajuridico.backend.presentation.controllers;

import com.sistemajuridico.backend.core.domain.Documento;
import com.sistemajuridico.backend.core.domain.exceptions.RegraNegocioException;
import com.sistemajuridico.backend.core.usecases.*;
import com.sistemajuridico.backend.presentation.dtos.DocumentoDTO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/documentos")
public class DocumentoController {

    private final UploadDocumentoUseCase uploadDocumentoUseCase;
    private final ListarDocumentosPorClienteUseCase listarDocumentosPorClienteUseCase;
    private final ListarDocumentosPorProcessoUseCase listarDocumentosPorProcessoUseCase;
    private final BuscarDocumentoPorIdUseCase buscarDocumentoPorIdUseCase;
    private final DownloadDocumentoUseCase downloadDocumentoUseCase;
    private final ExcluirDocumentoUseCase excluirDocumentoUseCase;

    public DocumentoController(UploadDocumentoUseCase uploadDocumentoUseCase,
                               ListarDocumentosPorClienteUseCase listarDocumentosPorClienteUseCase,
                               ListarDocumentosPorProcessoUseCase listarDocumentosPorProcessoUseCase,
                               BuscarDocumentoPorIdUseCase buscarDocumentoPorIdUseCase,
                               DownloadDocumentoUseCase downloadDocumentoUseCase,
                               ExcluirDocumentoUseCase excluirDocumentoUseCase) {
        this.uploadDocumentoUseCase = uploadDocumentoUseCase;
        this.listarDocumentosPorClienteUseCase = listarDocumentosPorClienteUseCase;
        this.listarDocumentosPorProcessoUseCase = listarDocumentosPorProcessoUseCase;
        this.buscarDocumentoPorIdUseCase = buscarDocumentoPorIdUseCase;
        this.downloadDocumentoUseCase = downloadDocumentoUseCase;
        this.excluirDocumentoUseCase = excluirDocumentoUseCase;
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

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<DocumentoDTO>> listarPorCliente(@PathVariable UUID clienteId) {
        List<DocumentoDTO> documentos = this.listarDocumentosPorClienteUseCase.executar(clienteId);
        return ResponseEntity.ok(documentos);
    }

    @GetMapping("/processo/{processoId}")
    public ResponseEntity<List<DocumentoDTO>> listarPorProcesso(@PathVariable UUID processoId) {
        List<DocumentoDTO> documentos = this.listarDocumentosPorProcessoUseCase.executar(processoId);
        return ResponseEntity.ok(documentos);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable UUID id) {
        Documento documento = this.buscarDocumentoPorIdUseCase.executar(id);
        byte[] arquivoBytes = this.downloadDocumentoUseCase.executar(id);

        String nomeArquivo = documento.getNomeArquivo();
        if (nomeArquivo == null || nomeArquivo.trim().isEmpty()) {
            nomeArquivo = "documento";
        }

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        Optional<MediaType> optMediaType = MediaTypeFactory.getMediaType(nomeArquivo);
        if (optMediaType.isPresent()) {
            mediaType = optMediaType.get();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomeArquivo + "\"")
                .contentType(mediaType)
                .body(arquivoBytes);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        this.excluirDocumentoUseCase.executar(id);
        return ResponseEntity.noContent().build();
    }
}
