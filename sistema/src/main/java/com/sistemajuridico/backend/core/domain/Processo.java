package com.sistemajuridico.backend.core.domain;

import com.sistemajuridico.backend.core.domain.enums.FaseProcessualEnum;
import com.sistemajuridico.backend.core.domain.enums.PapelClienteEnum;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "tb_processo")
public class Processo extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "numero_cnj", unique = true, nullable = false)
    private String numeroCnj;

    private String assunto;

    @Enumerated(EnumType.STRING)
    @Column(name = "fase_atual")
    private FaseProcessualEnum faseAtual;

    @Column(name = "parte_adversa")
    private String parteAdversa;

    @Column(name = "cpf_cnpj_parte_adversa")
    private String cpfCnpjParteAdversa;

    @Enumerated(EnumType.STRING)
    @Column(name = "papel_cliente")
    private PapelClienteEnum papelCliente;

    @Column(name = "valor_causa", precision = 15, scale = 2)
    private BigDecimal valorCausa;

    private String comarca;

    @Column(name = "data_criacao")
    private LocalDate dataCriacao = LocalDate.now();

    // Relacionamento N:1 (Muitos processos para um cliente)
    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario advogado;

    // Relacionamento 1:N (Um processo para muitos documentos)
    @ToString.Exclude
    @OneToMany(mappedBy = "processo", cascade = CascadeType.ALL)
    private List<Documento> documentos;

    // Relacionamento 1:N (Um processo para muitas tarefas)
    @ToString.Exclude
    @OneToMany(mappedBy = "processo", cascade = CascadeType.ALL)
    private List<Tarefa> tarefas;
}
