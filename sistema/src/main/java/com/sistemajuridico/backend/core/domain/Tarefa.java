package com.sistemajuridico.backend.core.domain;

import com.sistemajuridico.backend.core.domain.enums.TipoTarefaEnum;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "tb_tarefa")
public class Tarefa extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String descricao;

    @Column(name = "data_vencimento", nullable = false)
    private LocalDate dataVencimento;

    @Column(nullable = false)
    private Boolean concluida = false;

    @Enumerated(EnumType.STRING)
    private TipoTarefaEnum tipo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "processo_id")
    private Processo processo;
}
