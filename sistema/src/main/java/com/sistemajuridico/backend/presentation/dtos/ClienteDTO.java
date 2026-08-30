package com.sistemajuridico.backend.presentation.dtos;

import com.sistemajuridico.backend.core.domain.Cliente;
import com.sistemajuridico.backend.core.domain.enums.EstadoCivilEnum;
import com.sistemajuridico.backend.core.domain.enums.SexoEnum;
import com.sistemajuridico.backend.core.domain.enums.TipoClienteEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;
import java.util.UUID;

public record ClienteDTO(
        UUID id,

        @NotBlank(message = "O nome é obrigatório")
        String nome,

        @NotNull(message = "O tipo de cliente é obrigatório (FISICA ou JURIDICA)")
        TipoClienteEnum tipo,

        @NotBlank(message = "O CPF/CNPJ é obrigatório")
        String cpfCnpj,

        @Past(message = "A data de nascimento deve ser uma data no passado")
        LocalDate dataNascimento,

        EstadoCivilEnum estadoCivil,

        SexoEnum sexo,

        String profissao,

        String telefone,

        @Email(message = "Formato de e-mail inválido")
        String email,

        String cep,

        String logradouro,

        String numero,

        String complemento,

        String bairro,

        String cidade,

        String uf
) {

    public Cliente toEntity() {
        Cliente cliente = new Cliente();
        cliente.setId(this.id());
        cliente.setNome(this.nome());
        cliente.setTipo(this.tipo());
        cliente.setCpfCnpj(this.cpfCnpj());
        cliente.setDataNascimento(this.dataNascimento());
        cliente.setEstadoCivil(this.estadoCivil());
        cliente.setSexo(this.sexo());
        cliente.setProfissao(this.profissao());
        cliente.setTelefone(this.telefone());
        cliente.setEmail(this.email());
        cliente.setCep(this.cep());
        cliente.setLogradouro(this.logradouro());
        cliente.setNumero(this.numero());
        cliente.setComplemento(this.complemento());
        cliente.setBairro(this.bairro());
        cliente.setCidade(this.cidade());
        cliente.setUf(this.uf());
        return cliente;
    }

    public static ClienteDTO fromEntity(Cliente cliente) {
        return new ClienteDTO(
                cliente.getId(),
                cliente.getNome(),
                cliente.getTipo(),
                cliente.getCpfCnpj(),
                cliente.getDataNascimento(),
                cliente.getEstadoCivil(),
                cliente.getSexo(),
                cliente.getProfissao(),
                cliente.getTelefone(),
                cliente.getEmail(),
                cliente.getCep(),
                cliente.getLogradouro(),
                cliente.getNumero(),
                cliente.getComplemento(),
                cliente.getBairro(),
                cliente.getCidade(),
                cliente.getUf()
        );
    }
}
