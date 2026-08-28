package com.juridia.sistema.core.usecases;

import com.juridia.sistema.core.domain.Cliente;
import com.juridia.sistema.infrastructure.persistence.ClienteRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ListarClientesUseCase {

    private final ClienteRepository repository;

    public ListarClientesUseCase(ClienteRepository repository) {
        this.repository = repository;
    }

    public List<Cliente> executar() {
        return repository.findAll();
    }
}