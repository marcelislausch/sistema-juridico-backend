package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Cliente;
import com.sistemajuridico.backend.infrastructure.persistence.ClienteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ListarClientesUseCase {

    private final ClienteRepository repository;

    public ListarClientesUseCase(ClienteRepository repository) {
        this.repository = repository;
    }

    public Page<Cliente> executar(String termoBusca, Pageable pageable) {
        if (termoBusca != null && !termoBusca.trim().isEmpty()) {
            String termo = termoBusca.trim();
            return this.repository.buscarPorTermo(termo, pageable);
        } else {
            return this.repository.findAll(pageable);
        }
    }

    public Page<Cliente> executar(Pageable pageable) {
        return executar(null, pageable);
    }
}
