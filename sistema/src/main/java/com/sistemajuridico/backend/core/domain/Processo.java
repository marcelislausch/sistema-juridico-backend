package com.sistemajuridico.backend.core.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Entity
@Table(name = "tb_processo")
public class Processo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "numero_cnj", unique = true, nullable = false)
    private String numeroCnj;

    private String assunto;

    @Column(name = "fase_atual")
    private String faseAtual;

    @Column(name = "data_criacao")
    private LocalDate dataCriacao = LocalDate.now();

    // Relacionamento N:1 (Muitos processos para um cliente)
    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario advogado;
}
