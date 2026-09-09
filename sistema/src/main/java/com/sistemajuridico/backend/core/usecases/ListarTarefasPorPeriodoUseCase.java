package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Tarefa;
import com.sistemajuridico.backend.core.domain.Usuario;
import com.sistemajuridico.backend.core.domain.enums.PerfilAcessoEnum;
import com.sistemajuridico.backend.core.domain.enums.TipoTarefaEnum;
import com.sistemajuridico.backend.core.domain.exceptions.RegraNegocioException;
import com.sistemajuridico.backend.infrastructure.persistence.TarefaRepository;
import com.sistemajuridico.backend.presentation.dtos.TarefaDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ListarTarefasPorPeriodoUseCase {

    private final TarefaRepository tarefaRepository;

    public ListarTarefasPorPeriodoUseCase(TarefaRepository tarefaRepository) {
        this.tarefaRepository = tarefaRepository;
    }

    public List<TarefaDTO> executar(Usuario usuarioLogado,
                                    LocalDate inicio,
                                    LocalDate fim,
                                    Boolean concluida,
                                    TipoTarefaEnum tipo,
                                    UUID processoId,
                                    UUID responsavelId) {
        if (usuarioLogado == null) {
            throw new RegraNegocioException("Usuário autenticado não informado para a consulta.");
        }

        if (inicio != null && fim != null && inicio.isAfter(fim)) {
            throw new RegraNegocioException("A data inicial não pode ser posterior à data final.");
        }

        UUID filtroUsuarioId;
        if (usuarioLogado.getPerfil() == PerfilAcessoEnum.ADMIN || usuarioLogado.getPerfil() == PerfilAcessoEnum.SECRETARIA) {
            filtroUsuarioId = responsavelId;
        } else {
            filtroUsuarioId = usuarioLogado.getId();
        }

        String tipoStr = null;
        if (tipo != null) {
            tipoStr = tipo.name();
        }

        List<Tarefa> tarefas = this.tarefaRepository.buscarAgenda(filtroUsuarioId, inicio, fim, concluida, tipoStr, processoId);
        List<TarefaDTO> resultado = new ArrayList<>();
        for (Tarefa tarefa : tarefas) {
            resultado.add(TarefaDTO.fromEntity(tarefa));
        }

        return resultado;
    }

    public List<TarefaDTO> executar(UUID usuarioId, LocalDate inicio, LocalDate fim) {
        if (usuarioId == null) {
            throw new RegraNegocioException("O ID do usuário é obrigatório para a consulta de tarefas.");
        }

        if (inicio == null || fim == null) {
            throw new RegraNegocioException("As datas de início e fim são obrigatórias para a consulta de tarefas.");
        }

        if (inicio.isAfter(fim)) {
            throw new RegraNegocioException("A data inicial não pode ser posterior à data final.");
        }

        List<Tarefa> tarefas = this.tarefaRepository.findByUsuarioIdAndDataVencimentoBetween(usuarioId, inicio, fim);
        List<TarefaDTO> resultado = new ArrayList<>();
        for (Tarefa tarefa : tarefas) {
            resultado.add(TarefaDTO.fromEntity(tarefa));
        }

        return resultado;
    }
}