package com.sistemajuridico.backend.core.domain;

import com.sistemajuridico.backend.core.domain.enums.NaturezaFaturamentoEnum;
import com.sistemajuridico.backend.core.domain.enums.StatusFaturamentoEnum;
import com.sistemajuridico.backend.core.domain.enums.TipoFaturamentoEnum;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "tb_faturamento")
public class Faturamento extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String descricao;

    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    private TipoFaturamentoEnum tipo;

    @Enumerated(EnumType.STRING)
    private StatusFaturamentoEnum status;

    @Enumerated(EnumType.STRING)
    private NaturezaFaturamentoEnum natureza;

    @Column(name = "data_vencimento")
    private LocalDate dataVencimento;

    @Column(name = "data_pagamento")
    private LocalDate dataPagamento;

    @ManyToOne
    @JoinColumn(name = "processo_id")
    private Processo processo;
}
