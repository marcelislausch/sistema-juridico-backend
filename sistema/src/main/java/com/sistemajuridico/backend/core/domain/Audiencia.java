package com.sistemajuridico.backend.core.domain;

import com.sistemajuridico.backend.core.domain.enums.StatusAudienciaEnum;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "tb_audiencia")
public class Audiencia extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @Column(nullable = false)
    private String local; // Ex: 1ª Vara Cível de Ijuí ou Link do Teams/Zoom

    private String observacoes;

    @Enumerated(EnumType.STRING)
    private StatusAudienciaEnum status;

    @Column(name = "resumo_preparatorio_ia", columnDefinition = "TEXT")
    private String resumoPreparatorioIa;

    @ManyToOne
    @JoinColumn(name = "processo_id", nullable = false)
    private Processo processo;
}
