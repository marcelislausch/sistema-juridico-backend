package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Faturamento;
import com.sistemajuridico.backend.core.domain.Processo;
import com.sistemajuridico.backend.core.domain.Usuario;
import com.sistemajuridico.backend.core.domain.enums.PerfilAcessoEnum;
import com.sistemajuridico.backend.core.domain.enums.StatusFaturamentoEnum;
import com.sistemajuridico.backend.infrastructure.persistence.FaturamentoRepository;
import com.sistemajuridico.backend.infrastructure.persistence.ProcessoRepository;
import com.sistemajuridico.backend.infrastructure.persistence.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ArquivarProcessoUseCase {

    private final ProcessoRepository processoRepository;
    private final UsuarioRepository usuarioRepository;
    private final FaturamentoRepository faturamentoRepository;

    public ArquivarProcessoUseCase(ProcessoRepository processoRepository,
                                  UsuarioRepository usuarioRepository,
                                  FaturamentoRepository faturamentoRepository) {
        this.processoRepository = processoRepository;
        this.usuarioRepository = usuarioRepository;
        this.faturamentoRepository = faturamentoRepository;
    }

    public Processo executar(UUID processoId, UUID usuarioLogadoId) {
        Optional<Processo> processoBusca = processoRepository.findById(processoId);
        if (!processoBusca.isPresent()) {
            throw new RuntimeException("Processo não encontrado no sistema!");
        }

        Optional<Usuario> usuarioBusca = usuarioRepository.findById(usuarioLogadoId);
        if (!usuarioBusca.isPresent()) {
            throw new RuntimeException("Usuário não encontrado no sistema!");
        }

        Usuario usuario = usuarioBusca.get();
        if (usuario.getPerfil() != PerfilAcessoEnum.ADVOGADO && usuario.getPerfil() != PerfilAcessoEnum.ADMIN) {
            throw new RuntimeException("Acesso negado: Apenas advogados ou administradores podem arquivar processos.");
        }

        List<Faturamento> pendentes = faturamentoRepository.findByProcessoIdAndStatus(processoId, StatusFaturamentoEnum.PENDENTE);
        if (!pendentes.isEmpty()) {
            throw new RuntimeException("Operação bloqueada: Este processo possui faturamentos pendentes de pagamento.");
        }

        Processo processo = processoBusca.get();
        processo.setFaseAtual("ARQUIVADO");

        return processoRepository.save(processo);
    }
}

