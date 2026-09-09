package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Cliente;
import com.sistemajuridico.backend.core.domain.Processo;
import com.sistemajuridico.backend.core.domain.Usuario;
import com.sistemajuridico.backend.core.domain.enums.TipoItemBuscaEnum;
import com.sistemajuridico.backend.infrastructure.persistence.ClienteRepository;
import com.sistemajuridico.backend.infrastructure.persistence.ProcessoRepository;
import com.sistemajuridico.backend.infrastructure.persistence.UsuarioRepository;
import com.sistemajuridico.backend.presentation.dtos.ItemBuscaDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class BuscaGlobalUseCase {

    private final ProcessoRepository processoRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;

    public BuscaGlobalUseCase(ProcessoRepository processoRepository,
                              ClienteRepository clienteRepository,
                              UsuarioRepository usuarioRepository) {
        this.processoRepository = processoRepository;
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<ItemBuscaDTO> executar(String q, List<TipoItemBuscaEnum> tipos, int limit) {
        if (q == null || q.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String termo = q.trim();
        int maxItens = limit > 0 ? limit : 10;

        boolean pesquisarTodos = tipos == null || tipos.isEmpty();
        boolean incluirProcessos = pesquisarTodos || tipos.contains(TipoItemBuscaEnum.PROCESSO);
        boolean incluirClientes = pesquisarTodos || tipos.contains(TipoItemBuscaEnum.CLIENTE);
        boolean incluirUsuarios = pesquisarTodos || tipos.contains(TipoItemBuscaEnum.USUARIO);

        List<ItemBuscaDTO> resultados = new ArrayList<>();

        // 1. Processos
        if (incluirProcessos && resultados.size() < maxItens) {
            int restante = maxItens - resultados.size();
            Page<Processo> processos = this.processoRepository.buscarPorTermo(termo, PageRequest.of(0, restante));
            for (Processo p : processos.getContent()) {
                String subtitulo = p.getAssunto() != null ? p.getAssunto() : "";
                if (p.getCliente() != null && p.getCliente().getNome() != null) {
                    if (!subtitulo.isEmpty()) {
                        subtitulo = subtitulo + " • " + p.getCliente().getNome();
                    } else {
                        subtitulo = p.getCliente().getNome();
                    }
                }

                ItemBuscaDTO item = new ItemBuscaDTO(
                        p.getId(),
                        TipoItemBuscaEnum.PROCESSO,
                        p.getNumeroCnj(),
                        subtitulo,
                        "/processos/" + p.getId()
                );
                resultados.add(item);
                if (resultados.size() >= maxItens) {
                    break;
                }
            }
        }

        // 2. Clientes
        if (incluirClientes && resultados.size() < maxItens) {
            int restante = maxItens - resultados.size();
            Page<Cliente> clientes = this.clienteRepository.buscarPorTermo(termo, PageRequest.of(0, restante));
            for (Cliente c : clientes.getContent()) {
                String subtitulo = "";
                if (c.getCpfCnpj() != null && !c.getCpfCnpj().isEmpty()) {
                    subtitulo = "Doc: " + c.getCpfCnpj();
                } else if (c.getEmail() != null && !c.getEmail().isEmpty()) {
                    subtitulo = c.getEmail();
                }

                ItemBuscaDTO item = new ItemBuscaDTO(
                        c.getId(),
                        TipoItemBuscaEnum.CLIENTE,
                        c.getNome(),
                        subtitulo,
                        "/clientes/" + c.getId()
                );
                resultados.add(item);
                if (resultados.size() >= maxItens) {
                    break;
                }
            }
        }

        // 3. Usuários / Equipe
        if (incluirUsuarios && resultados.size() < maxItens) {
            int restante = maxItens - resultados.size();
            Page<Usuario> usuarios = this.usuarioRepository.buscarComFiltros(true, termo, PageRequest.of(0, restante));
            for (Usuario u : usuarios.getContent()) {
                String subtitulo = u.getPerfil() != null ? u.getPerfil().name() : "";
                if (u.getOab() != null && !u.getOab().isEmpty()) {
                    subtitulo = subtitulo + " • OAB: " + u.getOab();
                }

                ItemBuscaDTO item = new ItemBuscaDTO(
                        u.getId(),
                        TipoItemBuscaEnum.USUARIO,
                        u.getNome(),
                        subtitulo,
                        "/configuracoes"
                );
                resultados.add(item);
                if (resultados.size() >= maxItens) {
                    break;
                }
            }
        }

        return resultados;
    }
}
