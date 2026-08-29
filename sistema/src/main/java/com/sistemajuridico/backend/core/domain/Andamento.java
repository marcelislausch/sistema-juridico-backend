package com.sistemajuridico.backend.core.domain;

import com.sistemajuridico.backend.core.domain.enums.TipoAndamentoEnum;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "tb_andamento")
public class Andamento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "data_hora")
    private LocalDateTime dataHora = LocalDateTime.now();

    private String descricao;

    @Enumerated(EnumType.STRING)
    private TipoAndamentoEnum tipo;

    @ManyToOne
    @JoinColumn(name = "processo_id", nullable = false)
    private Processo processo;
}

