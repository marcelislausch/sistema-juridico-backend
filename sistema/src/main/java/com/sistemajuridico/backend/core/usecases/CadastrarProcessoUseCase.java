package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Cliente;
import com.sistemajuridico.backend.core.domain.Processo;
import com.sistemajuridico.backend.core.domain.Usuario;
import com.sistemajuridico.backend.core.domain.enums.PerfilAcessoEnum;
import com.sistemajuridico.backend.core.domain.exceptions.RecursoNaoEncontradoException;
import com.sistemajuridico.backend.core.domain.exceptions.RegraNegocioException;
import com.sistemajuridico.backend.infrastructure.persistence.ClienteRepository;
import com.sistemajuridico.backend.infrastructure.persistence.ProcessoRepository;
import com.sistemajuridico.backend.infrastructure.persistence.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class CadastrarProcessoUseCase {

    private final ProcessoRepository processoRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;

    public CadastrarProcessoUseCase(ProcessoRepository processoRepository,
                                    ClienteRepository clienteRepository,
                                    UsuarioRepository usuarioRepository) {
        this.processoRepository = processoRepository;
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public Processo executar(Processo processo, UUID clienteId, UUID advogadoId) {
        Optional<Cliente> optCliente = clienteRepository.findById(clienteId);
        if (optCliente.isEmpty()) {
            throw new RecursoNaoEncontradoException("Cliente não encontrado no sistema!");
        }
        Cliente cliente = optCliente.get();
        processo.setCliente(cliente);

        if (advogadoId != null) {
            Optional<Usuario> optAdvogado = usuarioRepository.findById(advogadoId);
            if (optAdvogado.isEmpty()) {
                throw new RecursoNaoEncontradoException("Advogado não encontrado no sistema!");
            }
            Usuario advogado = optAdvogado.get();

            if (advogado.getPerfil() != PerfilAcessoEnum.ADVOGADO && advogado.getPerfil() != PerfilAcessoEnum.ADMIN) {
                throw new RegraNegocioException("O usuário selecionado não possui perfil de Advogado.");
            }
            processo.setAdvogado(advogado);
        }

        return processoRepository.save(processo);
    }
}
