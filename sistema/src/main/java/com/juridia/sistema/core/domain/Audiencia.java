package com.juridia.sistema.core.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "tb_audiencia")
public class Audiencia {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @Column(nullable = false)
    private String local; // Ex: 1ª Vara Cível de Ijuí ou Link do Teams/Zoom

    private String observacoes;

    @ManyToOne
    @JoinColumn(name = "processo_id", nullable = false)
    private Processo processo;
}