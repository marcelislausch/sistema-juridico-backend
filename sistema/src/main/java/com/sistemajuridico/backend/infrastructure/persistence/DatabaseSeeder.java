package com.sistemajuridico.backend.infrastructure.persistence;

import com.sistemajuridico.backend.core.domain.Usuario;
import com.sistemajuridico.backend.core.domain.enums.PerfilAcessoEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${api.admin.email}")
    private String adminEmail;

    @Value("${api.admin.senha}")
    private String adminSenha;

    public DatabaseSeeder(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        List<Usuario> admins = usuarioRepository.findByPerfil(PerfilAcessoEnum.ADMIN);
        if (admins.isEmpty()) {
            Usuario admin = new Usuario();
            admin.setNome("Administrador do Sistema");
            admin.setEmail(this.adminEmail);
            admin.setSenhaHash(this.passwordEncoder.encode(this.adminSenha));
            admin.setPerfil(PerfilAcessoEnum.ADMIN);
            admin.setAtivo(true);

            this.usuarioRepository.save(admin);
        }
    }
}
