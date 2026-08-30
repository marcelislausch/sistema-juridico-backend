package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Cliente;
import com.sistemajuridico.backend.core.domain.exceptions.RecursoNaoEncontradoException;
import com.sistemajuridico.backend.core.domain.exceptions.RegraNegocioException;
import com.sistemajuridico.backend.core.domain.validators.DocumentoValidator;
import com.sistemajuridico.backend.infrastructure.persistence.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AtualizarClienteUseCase {

    private final ClienteRepository repository;

    public AtualizarClienteUseCase(ClienteRepository repository) {
        this.repository = repository;
    }

    public Cliente executar(UUID id, Cliente dadosAtualizados) {
        Optional<Cliente> optCliente = repository.findById(id);
        if (optCliente.isEmpty()) {
            throw new RecursoNaoEncontradoException("Cliente não encontrado no sistema!");
        }

        Cliente clienteExistente = optCliente.get();

        if (dadosAtualizados.getCpfCnpj() != null) {
            String cpfCnpjLimpo = dadosAtualizados.getCpfCnpj().replaceAll("[^0-9A-Za-z]", "").toUpperCase();
            if (!DocumentoValidator.isCpfCnpjValido(cpfCnpjLimpo)) {
                throw new RegraNegocioException("CPF/CNPJ inválido! Verifique os dados informados.");
            }
            clienteExistente.setCpfCnpj(cpfCnpjLimpo);
        }

        clienteExistente.setNome(dadosAtualizados.getNome());
        clienteExistente.setTipo(dadosAtualizados.getTipo());
        clienteExistente.setDataNascimento(dadosAtualizados.getDataNascimento());
        clienteExistente.setTelefone(dadosAtualizados.getTelefone());
        clienteExistente.setEmail(dadosAtualizados.getEmail());
        clienteExistente.setCep(dadosAtualizados.getCep());
        clienteExistente.setLogradouro(dadosAtualizados.getLogradouro());
        clienteExistente.setNumero(dadosAtualizados.getNumero());
        clienteExistente.setComplemento(dadosAtualizados.getComplemento());
        clienteExistente.setBairro(dadosAtualizados.getBairro());
        clienteExistente.setCidade(dadosAtualizados.getCidade());
        clienteExistente.setUf(dadosAtualizados.getUf());

        return repository.save(clienteExistente);
    }
}
