package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Cliente;
import com.sistemajuridico.backend.core.domain.enums.TipoClienteEnum;
import com.sistemajuridico.backend.infrastructure.persistence.ClienteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListarClientesUseCase {

    private final ClienteRepository repository;

    public ListarClientesUseCase(ClienteRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<Cliente> executar(TipoClienteEnum tipo, String termoBusca, Pageable pageable) {
        String termo = null;
        if (termoBusca != null && !termoBusca.trim().isEmpty()) {
            termo = termoBusca.trim();
        }
        String tipoStr = null;
        if (tipo != null) {
            tipoStr = tipo.name();
        }
        return this.repository.buscarComFiltros(tipoStr, termo, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Cliente> executar(String termoBusca, Pageable pageable) {
        return executar(null, termoBusca, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Cliente> executar(Pageable pageable) {
        return executar(null, null, pageable);
    }
}
