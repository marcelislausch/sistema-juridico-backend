package com.sistemajuridico.backend.infrastructure.storage;

public interface StorageService {
    String salvarArquivo(String nomeOriginal, byte[] dados);
    byte[] downloadArquivo(String idArquivo);
    void excluirArquivo(String idArquivo);
}

