package com.sistemajuridico.backend.core.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "tb_intimacao_pje")
public class IntimacaoPje extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "comunicacao_id", unique = true, nullable = false)
    private Long comunicacaoId;

    @Column(length = 255)
    private String hash;

    @Column(name = "numero_processo", length = 30)
    private String numeroProcesso;

    @Column(name = "numero_processo_mascara", length = 40)
    private String numeroProcessoMascara;

    @Column(name = "sigla_tribunal", length = 30)
    private String siglaTribunal;

    @Column(name = "tipo_comunicacao", length = 50)
    private String tipoComunicacao;

    @Column(name = "tipo_documento", length = 100)
    private String tipoDocumento;

    @Column(name = "nome_orgao", length = 255)
    private String nomeOrgao;

    @Column(name = "data_disponibilizacao")
    private LocalDate dataDisponibilizacao;

    @Column(columnDefinition = "TEXT")
    private String texto;

    @Column(length = 1000)
    private String link;

    @Column(name = "numero_oab", length = 20)
    private String numeroOab;

    @Column(name = "uf_oab", length = 5)
    private String ufOab;

    private boolean lida = false;

    @Column(name = "data_sincronizacao")
    private LocalDateTime dataSincronizacao = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processo_id")
    private Processo processo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario advogado;
}
