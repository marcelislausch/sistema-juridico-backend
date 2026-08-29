package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Usuario;
import com.sistemajuridico.backend.core.domain.enums.PerfilAcessoEnum;
import com.sistemajuridico.backend.infrastructure.persistence.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListarAdvogadosUseCase {

    private final UsuarioRepository usuarioRepository;

    public ListarAdvogadosUseCase(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<Usuario> executar() {
        return usuarioRepository.findByPerfilAndAtivoTrue(PerfilAcessoEnum.ADVOGADO);
    }
}
