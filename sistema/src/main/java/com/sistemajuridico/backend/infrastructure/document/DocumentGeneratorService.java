package com.sistemajuridico.backend.infrastructure.document;

import com.sistemajuridico.backend.core.domain.Cliente;

public interface DocumentGeneratorService {
    byte[] gerarProcuracao(Cliente cliente);
    byte[] gerarContratoHonorarios(Cliente cliente);
}
