package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Cliente;
import com.sistemajuridico.backend.infrastructure.document.DocumentGeneratorService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GerarContratoHonorariosUseCase {

    private final BuscarClientePorIdUseCase buscarClientePorIdUseCase;
    private final DocumentGeneratorService documentGeneratorService;

    public GerarContratoHonorariosUseCase(BuscarClientePorIdUseCase buscarClientePorIdUseCase,
                                          DocumentGeneratorService documentGeneratorService) {
        this.buscarClientePorIdUseCase = buscarClientePorIdUseCase;
        this.documentGeneratorService = documentGeneratorService;
    }

    public byte[] executar(UUID clienteId, String acao, String vara, String comarca, String valorServicos, String objetivoDemanda) {
        Cliente cliente = this.buscarClientePorIdUseCase.executar(clienteId);
        return this.documentGeneratorService.gerarContratoHonorarios(cliente, acao, vara, comarca, valorServicos, objetivoDemanda);
    }
}
