package com.sistemajuridico.backend.infrastructure.storage;

public interface StorageService {
    String salvarArquivo(String nomeOriginal, byte[] dados);
}
