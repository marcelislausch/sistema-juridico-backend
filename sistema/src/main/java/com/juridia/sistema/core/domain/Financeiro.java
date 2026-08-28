package com.juridia.sistema.core.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "tb_financeiro")
public class Financeiro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String descricao; // Ex: Honorários Contratuais - Parcela 1/3 ou Valor da Causa

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoLancamento tipo; // RECEITA, DESPESA ou VALOR_ACAO

    @Column(name = "data_vencimento", nullable = false)
    private LocalDate dataVencimento;

    private boolean pago = false;

    @ManyToOne
    @JoinColumn(name = "processo_id", nullable = true)
    private Processo processo; // O financeiro pode estar atrelado a um processo ou avulso ao cliente
}