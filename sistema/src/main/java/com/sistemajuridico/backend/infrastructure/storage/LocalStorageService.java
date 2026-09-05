package com.sistemajuridico.backend.infrastructure.storage;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class LocalStorageService implements StorageService {

    private final Path rootPath;

    public LocalStorageService() {
        this.rootPath = Paths.get("uploads", "documentos");
        try {
            Files.createDirectories(this.rootPath);
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível criar o diretório de upload.", e);
        }
    }

    @Override
    public String salvarArquivo(String nomeOriginal, byte[] dados) {
        String nomeLimpo = "arquivo";
        if (nomeOriginal != null && !nomeOriginal.trim().isEmpty()) {
            nomeLimpo = nomeOriginal.replaceAll("[^a-zA-Z0-9._-]", "_");
        }

        String nomeUnico = UUID.randomUUID().toString() + "_" + nomeLimpo;
        Path caminhoDestino = this.rootPath.resolve(nomeUnico);

        try {
            Files.write(caminhoDestino, dados);
            return caminhoDestino.toString();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar o arquivo no armazenamento local.", e);
        }
    }

    @Override
    public byte[] downloadArquivo(String caminho) {
        if (caminho == null || caminho.trim().isEmpty()) {
            throw new RuntimeException("O caminho do arquivo local não pode ser nulo ou vazio.");
        }
        Path caminhoArquivo = Paths.get(caminho);
        if (!Files.exists(caminhoArquivo)) {
            throw new RuntimeException("Arquivo físico não encontrado no armazenamento local: " + caminho);
        }
        try {
            return Files.readAllBytes(caminhoArquivo);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao ler o arquivo no armazenamento local: " + e.getMessage(), e);
        }
    }

    @Override
    public void excluirArquivo(String caminho) {
        if (caminho == null || caminho.trim().isEmpty()) {
            return;
        }
        Path caminhoArquivo = Paths.get(caminho);
        try {
            if (Files.exists(caminhoArquivo)) {
                Files.delete(caminhoArquivo);
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao excluir o arquivo no armazenamento local: " + e.getMessage(), e);
        }
    }
}
