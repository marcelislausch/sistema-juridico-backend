package com.sistemajuridico.backend.core.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "tb_documento")
public class Documento extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nomeArquivo;

    private String titulo;

    @Column(nullable = false)
    private String caminhoStorage;

    @Column(nullable = false)
    private Boolean indexadoIA = false;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "processo_id")
    private Processo processo;
}
