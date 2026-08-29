package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Cliente;
import com.sistemajuridico.backend.core.domain.Processo;
import com.sistemajuridico.backend.core.domain.Usuario;
import com.sistemajuridico.backend.core.domain.enums.PerfilAcessoEnum;
import com.sistemajuridico.backend.infrastructure.persistence.ClienteRepository;
import com.sistemajuridico.backend.infrastructure.persistence.ProcessoRepository;
import com.sistemajuridico.backend.infrastructure.persistence.UsuarioRepository;
import org.springframework.stereotype.Service;

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
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado no sistema!"));
        processo.setCliente(cliente);

        if (advogadoId != null) {
            Usuario advogado = usuarioRepository.findById(advogadoId)
                    .orElseThrow(() -> new RuntimeException("Advogado não encontrado no sistema!"));

            if (advogado.getPerfil() != PerfilAcessoEnum.ADVOGADO && advogado.getPerfil() != PerfilAcessoEnum.ADMIN) {
                throw new RuntimeException("O usuário selecionado não possui perfil de Advogado.");
            }
            processo.setAdvogado(advogado);
        }

        return processoRepository.save(processo);
    }
}
