package com.sistemajuridico.backend.infrastructure.persistence;

import com.sistemajuridico.backend.core.domain.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UsuarioRepositoryCustom {
    Page<Usuario> buscarComFiltros(Boolean ativo, String termo, Pageable pageable);
}
