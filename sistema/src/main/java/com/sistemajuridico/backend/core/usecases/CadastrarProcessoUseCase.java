package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Cliente;
import com.sistemajuridico.backend.core.domain.Processo;
import com.sistemajuridico.backend.infrastructure.persistence.ClienteRepository;
import com.sistemajuridico.backend.infrastructure.persistence.ProcessoRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class CadastrarProcessoUseCase {

    private final ProcessoRepository processoRepository;
    private final ClienteRepository clienteRepository;

    public CadastrarProcessoUseCase(ProcessoRepository processoRepository, ClienteRepository clienteRepository) {
        this.processoRepository = processoRepository;
        this.clienteRepository = clienteRepository;
    }

    public Processo executar(Processo processo, UUID clienteId) {

        Optional<Cliente> clienteBusca = clienteRepository.findById(clienteId);
        if (!clienteBusca.isPresent()) {
            throw new RuntimeException("Cliente não encontrado no sistema!");
        }
        Cliente cliente = clienteBusca.get();
        processo.setCliente(cliente);
        return processoRepository.save(processo);
    }
}
