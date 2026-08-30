package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Cliente;
import com.sistemajuridico.backend.core.domain.exceptions.RegraNegocioException;
import com.sistemajuridico.backend.core.domain.validators.DocumentoValidator;
import com.sistemajuridico.backend.infrastructure.persistence.ClienteRepository;
import org.springframework.stereotype.Service;

@Service
public class CadastrarClienteUseCase {

    private final ClienteRepository repository;

    public CadastrarClienteUseCase(ClienteRepository repository) {
        this.repository = repository;
    }

    public Cliente executar(Cliente cliente) {
        if (cliente.getCpfCnpj() != null) {
            cliente.setCpfCnpj(cliente.getCpfCnpj().replaceAll("[^0-9A-Za-z]", "").toUpperCase());
        }

        if (!DocumentoValidator.isCpfCnpjValido(cliente.getCpfCnpj())) {
            throw new RegraNegocioException("CPF/CNPJ inválido! Verifique os dados informados.");
        }

        return repository.save(cliente);
    }
}
