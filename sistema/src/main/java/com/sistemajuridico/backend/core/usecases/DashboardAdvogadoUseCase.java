package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Tarefa;
import com.sistemajuridico.backend.infrastructure.persistence.ClienteRepository;
import com.sistemajuridico.backend.infrastructure.persistence.ProcessoRepository;
import com.sistemajuridico.backend.infrastructure.persistence.TarefaRepository;
import com.sistemajuridico.backend.presentation.dtos.ResumoDashboardDTO;
import com.sistemajuridico.backend.presentation.dtos.TarefaDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DashboardAdvogadoUseCase {

    private final ClienteRepository clienteRepository;
    private final ProcessoRepository processoRepository;
    private final TarefaRepository tarefaRepository;

    public DashboardAdvogadoUseCase(ClienteRepository clienteRepository,
                                    ProcessoRepository processoRepository,
                                    TarefaRepository tarefaRepository) {
        this.clienteRepository = clienteRepository;
        this.processoRepository = processoRepository;
        this.tarefaRepository = tarefaRepository;
    }

    public ResumoDashboardDTO executar(UUID usuarioId) {
        int totalClientesAtivos = this.clienteRepository.findAll().size();
        int totalProcessosAndamento = this.processoRepository.findAll().size();

        List<Tarefa> pendentes = this.tarefaRepository.findByUsuarioIdAndConcluidaFalseOrderByDataVencimentoAsc(usuarioId);

        LocalDate hoje = LocalDate.now();
        int tarefasPendentesHoje = 0;
        for (Tarefa tarefa : pendentes) {
            if (hoje.equals(tarefa.getDataVencimento())) {
                tarefasPendentesHoje++;
            }
        }

        List<TarefaDTO> proximasTarefas = new ArrayList<>();
        int limite = Math.min(5, pendentes.size());
        for (int i = 0; i < limite; i++) {
            proximasTarefas.add(TarefaDTO.fromEntity(pendentes.get(i)));
        }

        return new ResumoDashboardDTO(
                totalClientesAtivos,
                totalProcessosAndamento,
                tarefasPendentesHoje,
                proximasTarefas
        );
    }
}
