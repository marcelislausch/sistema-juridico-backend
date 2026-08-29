package com.sistemajuridico.backend.core.domain;

import com.sistemajuridico.backend.core.domain.enums.TipoClienteEnum;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "tb_cliente")
public class Cliente extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nome;

    @Enumerated(EnumType.STRING)
    private TipoClienteEnum tipo;

    @Column(name = "cpf_cnpj", unique = true, nullable = false, length = 14)
    private String cpfCnpj;

    private String telefone;

    private String email;

    // Relacionamento 1:N (Um cliente para muitos processos)
    @ToString.Exclude
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
    private List<Processo> processos;
}
