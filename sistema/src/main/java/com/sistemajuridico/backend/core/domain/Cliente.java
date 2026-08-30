package com.sistemajuridico.backend.core.domain;

import com.sistemajuridico.backend.core.domain.enums.EstadoCivilEnum;
import com.sistemajuridico.backend.core.domain.enums.SexoEnum;
import com.sistemajuridico.backend.core.domain.enums.TipoClienteEnum;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;
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

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_civil")
    private EstadoCivilEnum estadoCivil;

    @Enumerated(EnumType.STRING)
    private SexoEnum sexo;

    private String profissao;

    private String telefone;

    private String email;

    private String cep;

    private String logradouro;

    private String numero;

    private String complemento;

    private String bairro;

    private String cidade;

    private String uf;

    // Relacionamento 1:N (Um cliente para muitos processos)
    @ToString.Exclude
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
    private List<Processo> processos;

    // Relacionamento 1:N (Um cliente para muitos documentos)
    @ToString.Exclude
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
    private List<Documento> documentos;
}
