package com.juridia.sistema.core.usecases;

import com.juridia.sistema.core.domain.Cliente;
import com.juridia.sistema.infrastructure.persistence.ClienteRepository;
import org.springframework.stereotype.Service;

@Service
public class CadastrarClienteUseCase {

    private final ClienteRepository repository;

    public CadastrarClienteUseCase(ClienteRepository repository) {
        this.repository = repository;
    }

    public Cliente executar(Cliente cliente) {
        return repository.save(cliente);
    }
}