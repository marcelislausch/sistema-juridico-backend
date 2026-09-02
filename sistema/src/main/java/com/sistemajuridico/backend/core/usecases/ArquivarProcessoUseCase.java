package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Faturamento;
import com.sistemajuridico.backend.core.domain.Processo;
import com.sistemajuridico.backend.core.domain.Usuario;
import com.sistemajuridico.backend.core.domain.enums.FaseProcessualEnum;
import com.sistemajuridico.backend.core.domain.enums.PerfilAcessoEnum;
import com.sistemajuridico.backend.core.domain.enums.StatusFaturamentoEnum;
import com.sistemajuridico.backend.core.domain.exceptions.RecursoNaoEncontradoException;
import com.sistemajuridico.backend.core.domain.exceptions.RegraNegocioException;
import com.sistemajuridico.backend.infrastructure.persistence.FaturamentoRepository;
import com.sistemajuridico.backend.infrastructure.persistence.ProcessoRepository;
import com.sistemajuridico.backend.infrastructure.persistence.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    public Processo executar(UUID processoId) {
        Optional<Processo> optProcesso = processoRepository.findById(processoId);
        if (optProcesso.isEmpty()) {
            throw new RecursoNaoEncontradoException("Processo não encontrado no sistema!");
        }
        Processo processo = optProcesso.get();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new RegraNegocioException("Usuário não autenticado no sistema!");
        }

        String emailLogado = authentication.getName();
        Optional<Usuario> optUsuario = usuarioRepository.findByEmail(emailLogado);
        if (optUsuario.isEmpty()) {
            throw new RecursoNaoEncontradoException("Usuário autenticado não encontrado no sistema!");
        }
        Usuario usuario = optUsuario.get();

        if (usuario.getPerfil() != PerfilAcessoEnum.ADVOGADO && usuario.getPerfil() != PerfilAcessoEnum.ADMIN) {
            throw new RegraNegocioException("Acesso negado: Apenas advogados ou administradores podem arquivar processos.");
        }

        List<Faturamento> pendentes = faturamentoRepository.findByProcessoIdAndStatus(processoId, StatusFaturamentoEnum.PENDENTE);
        if (!pendentes.isEmpty()) {
            throw new RegraNegocioException("Operação bloqueada: Este processo possui faturamentos pendentes de pagamento.");
        }

        processo.setFaseAtual(FaseProcessualEnum.ARQUIVADO);

        return processoRepository.save(processo);
    }
}
