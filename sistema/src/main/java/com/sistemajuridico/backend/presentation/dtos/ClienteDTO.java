package com.sistemajuridico.backend.presentation.dtos;

import com.sistemajuridico.backend.core.domain.Cliente;
import com.sistemajuridico.backend.core.domain.enums.TipoClienteEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ClienteDTO(
        UUID id,

        @NotBlank(message = "O nome é obrigatório")
        String nome,

        @NotNull(message = "O tipo de cliente é obrigatório (FISICA ou JURIDICA)")
        TipoClienteEnum tipo,

        @NotBlank(message = "O CPF/CNPJ é obrigatório")
        String cpfCnpj,

        String telefone,

        @Email(message = "Formato de e-mail inválido")
        String email
) {

    public Cliente toEntity() {
        Cliente cliente = new Cliente();
        cliente.setId(this.id());
        cliente.setNome(this.nome());
        cliente.setTipo(this.tipo());
        cliente.setCpfCnpj(this.cpfCnpj());
        cliente.setTelefone(this.telefone());
        cliente.setEmail(this.email());
        return cliente;
    }

    public static ClienteDTO fromEntity(Cliente cliente) {
        return new ClienteDTO(
                cliente.getId(),
                cliente.getNome(),
                cliente.getTipo(),
                cliente.getCpfCnpj(),
                cliente.getTelefone(),
                cliente.getEmail()
        );
    }
}
