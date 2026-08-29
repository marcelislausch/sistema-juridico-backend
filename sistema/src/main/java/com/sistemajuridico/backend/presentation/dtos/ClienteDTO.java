package com.sistemajuridico.backend.presentation.dtos;

import com.sistemajuridico.backend.core.domain.Cliente;
import com.sistemajuridico.backend.core.domain.enums.TipoClienteEnum;

import java.util.UUID;

public record ClienteDTO(
        UUID id,
        String nome,
        TipoClienteEnum tipo,
        String cpfCnpj,
        String telefone,
        String email
) {

    // Converte de DTO para Entidade (ignora o ID na hora de salvar)
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

    // Converte de Entidade para DTO (traz o ID gerado pelo banco)
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
