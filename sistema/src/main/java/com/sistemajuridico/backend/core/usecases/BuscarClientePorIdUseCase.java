package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Cliente;
import com.sistemajuridico.backend.infrastructure.persistence.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class BuscarClientePorIdUseCase {

    private final ClienteRepository clienteRepository;

    public BuscarClientePorIdUseCase(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public Cliente executar(UUID id) {
        Optional<Cliente> optionalCliente = this.clienteRepository.findById(id);
        if (optionalCliente.isEmpty()) {
            throw new RuntimeException("Cliente não encontrado com o ID: " + id);
        }
        return optionalCliente.get();
    }
}
