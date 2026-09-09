package com.sistemajuridico.backend.infrastructure.persistence;

import com.sistemajuridico.backend.core.domain.Escritorio;
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
    private final EscritorioRepository escritorioRepository;

    @Value("${api.admin.email}")
    private String adminEmail;

    @Value("${api.admin.senha}")
    private String adminSenha;

    public DatabaseSeeder(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder,
                          EscritorioRepository escritorioRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.escritorioRepository = escritorioRepository;
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

        List<Escritorio> escritorios = this.escritorioRepository.findAll();
        if (escritorios.isEmpty()) {
            Escritorio padrao = new Escritorio();
            padrao.setRazaoSocial("Cristhian Menezes Advocacia");
            padrao.setNomeFantasia("Cristhian Menezes Advocacia");
            padrao.setCnpj("");
            padrao.setRegistroOabSociedade("OAB/RS 121.837");
            padrao.setTelefone("5533321000");
            padrao.setWhatsapp("55999887766");
            padrao.setEmail("cristhian.menezes@outlook.com");
            padrao.setCep("98700000");
            padrao.setLogradouro("Rua Tiradentes");
            padrao.setNumero("676");
            padrao.setComplemento("Sala 01");
            padrao.setBairro("Centro");
            padrao.setCidade("Ijuí");
            padrao.setUf("RS");

            this.escritorioRepository.save(padrao);
        }
    }
}
