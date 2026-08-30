package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Cliente;
import com.sistemajuridico.backend.core.domain.exceptions.RecursoNaoEncontradoException;
import com.sistemajuridico.backend.infrastructure.document.DocumentGeneratorService;
import com.sistemajuridico.backend.infrastructure.persistence.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class GerarProcuracaoClienteUseCase {

    private final ClienteRepository clienteRepository;
    private final DocumentGeneratorService documentGeneratorService;

    public GerarProcuracaoClienteUseCase(ClienteRepository clienteRepository,
                                         DocumentGeneratorService documentGeneratorService) {
        this.clienteRepository = clienteRepository;
        this.documentGeneratorService = documentGeneratorService;
    }

    public byte[] executar(UUID clienteId) {
        Optional<Cliente> optCliente = this.clienteRepository.findById(clienteId);
        if (optCliente.isEmpty()) {
            throw new RecursoNaoEncontradoException("Cliente não encontrado no sistema!");
        }

        Cliente cliente = optCliente.get();
        return this.documentGeneratorService.gerarProcuracao(cliente);
    }
}
