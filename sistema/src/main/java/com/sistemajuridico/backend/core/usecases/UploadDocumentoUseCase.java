package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Cliente;
import com.sistemajuridico.backend.core.domain.Documento;
import com.sistemajuridico.backend.core.domain.Processo;
import com.sistemajuridico.backend.core.domain.exceptions.RecursoNaoEncontradoException;
import com.sistemajuridico.backend.core.domain.exceptions.RegraNegocioException;
import com.sistemajuridico.backend.infrastructure.persistence.ClienteRepository;
import com.sistemajuridico.backend.infrastructure.persistence.DocumentoRepository;
import com.sistemajuridico.backend.infrastructure.persistence.ProcessoRepository;
import com.sistemajuridico.backend.infrastructure.storage.StorageService;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UploadDocumentoUseCase {

    private final StorageService storageService;
    private final DocumentoRepository documentoRepository;
    private final ClienteRepository clienteRepository;
    private final ProcessoRepository processoRepository;

    public UploadDocumentoUseCase(StorageService storageService,
                                  DocumentoRepository documentoRepository,
                                  ClienteRepository clienteRepository,
                                  ProcessoRepository processoRepository) {
        this.storageService = storageService;
        this.documentoRepository = documentoRepository;
        this.clienteRepository = clienteRepository;
        this.processoRepository = processoRepository;
    }

    public Documento executar(byte[] arquivoBytes, String nomeOriginal, String titulo, UUID clienteId, UUID processoId) {
        if (arquivoBytes == null || arquivoBytes.length == 0) {
            throw new RegraNegocioException("O conteúdo do arquivo é obrigatório para upload.");
        }

        if (nomeOriginal == null || nomeOriginal.trim().isEmpty()) {
            throw new RegraNegocioException("O nome do arquivo é obrigatório.");
        }

        Cliente cliente = null;
        if (clienteId != null) {
            Optional<Cliente> optCliente = this.clienteRepository.findById(clienteId);
            if (optCliente.isEmpty()) {
                throw new RecursoNaoEncontradoException("Cliente não encontrado no sistema!");
            }
            cliente = optCliente.get();
        }

        Processo processo = null;
        if (processoId != null) {
            Optional<Processo> optProcesso = this.processoRepository.findById(processoId);
            if (optProcesso.isEmpty()) {
                throw new RecursoNaoEncontradoException("Processo não encontrado no sistema!");
            }
            processo = optProcesso.get();
        }

        String caminhoStorage = this.storageService.salvarArquivo(nomeOriginal, arquivoBytes);

        Documento documento = new Documento();
        documento.setNomeArquivo(nomeOriginal);
        documento.setTitulo(titulo);
        documento.setCaminhoStorage(caminhoStorage);
        documento.setIndexadoIA(false);
        documento.setCliente(cliente);
        documento.setProcesso(processo);

        return this.documentoRepository.save(documento);
    }
}
