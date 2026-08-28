package com.juridia.sistema.core.domain;

import com.juridia.sistema.core.domain.enums.PerfilAcesso;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "tb_usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nome;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "perfil_acesso", nullable = false)
    private PerfilAcesso perfil;

    @Column(unique = true)
    private String oab;

    private boolean ativo = true;
}