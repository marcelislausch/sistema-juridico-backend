package com.juridia.sistema.core.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "tb_prazo_processual")
public class PrazoProcessual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String descricao; // Ex: Contestação, Réplica, Apelação

    @Column(name = "data_fatal", nullable = false)
    private LocalDate dataFatal;

    private boolean concluido = false;

    @ManyToOne
    @JoinColumn(name = "processo_id", nullable = false)
    private Processo processo;
}