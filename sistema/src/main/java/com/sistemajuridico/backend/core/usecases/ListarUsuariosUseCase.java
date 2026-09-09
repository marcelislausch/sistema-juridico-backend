package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Usuario;
import com.sistemajuridico.backend.infrastructure.persistence.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListarUsuariosUseCase {

    private final UsuarioRepository usuarioRepository;

    public ListarUsuariosUseCase(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public Page<Usuario> executar(Boolean ativo, String q, Pageable pageable) {
        String termo = null;
        if (q != null && !q.trim().isEmpty()) {
            termo = q.trim();
        }
        return this.usuarioRepository.buscarComFiltros(ativo, termo, pageable);
    }
}
