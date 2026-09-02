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

    private final BuscarClientePorIdUseCase buscarClientePorIdUseCase;
    private final DocumentGeneratorService documentGeneratorService;

    public GerarProcuracaoClienteUseCase(BuscarClientePorIdUseCase buscarClientePorIdUseCase,
                                         DocumentGeneratorService documentGeneratorService) {
        this.buscarClientePorIdUseCase = buscarClientePorIdUseCase;
        this.documentGeneratorService = documentGeneratorService;
    }

    public byte[] executar(UUID clienteId, String acao, String varaCivel, String comarca) {
        Cliente cliente = this.buscarClientePorIdUseCase.executar(clienteId);
        return this.documentGeneratorService.gerarProcuracao(cliente, acao, varaCivel, comarca);
    }
}
