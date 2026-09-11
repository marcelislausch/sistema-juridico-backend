package com.sistemajuridico.backend.core.domain;

import com.sistemajuridico.backend.core.domain.enums.NaturezaFaturamentoEnum;
import com.sistemajuridico.backend.core.domain.enums.OrigemPagamentoEnum;
import com.sistemajuridico.backend.core.domain.enums.StatusFaturamentoEnum;
import com.sistemajuridico.backend.core.domain.enums.StatusRepasseEnum;
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

    @Column(precision = 15, scale = 2)
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

    @Column(name = "numero_parcela")
    private Integer numeroParcela;

    @Column(name = "total_parcelas")
    private Integer totalParcelas;

    @Enumerated(EnumType.STRING)
    @Column(name = "origem_pagamento")
    private OrigemPagamentoEnum origemPagamento;

    @Column(name = "valor_honorarios_retidos", precision = 15, scale = 2)
    private BigDecimal valorHonorariosRetidos;

    @Column(name = "valor_repasse_cliente", precision = 15, scale = 2)
    private BigDecimal valorRepasseCliente;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_repasse")
    private StatusRepasseEnum statusRepasse;

    @Column(name = "forma_repasse")
    private String formaRepasse;

    @Column(name = "dados_bancarios_cliente")
    private String dadosBancariosCliente;

    @Column(name = "data_repasse")
    private LocalDate dataRepasse;

    @ManyToOne
    @JoinColumn(name = "processo_id")
    private Processo processo;
}
