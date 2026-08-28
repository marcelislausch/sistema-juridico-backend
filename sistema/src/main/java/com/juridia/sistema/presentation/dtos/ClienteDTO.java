package com.juridia.sistema.presentation.dtos;

import com.juridia.sistema.core.domain.Cliente;

public record ClienteDTO(Long id, String nome, String cpfCnpj, String telefone, String email) {

    // Converte de DTO para Entidade (ignora o ID na hora de salvar)
    public Cliente toEntity() {
        Cliente cliente = new Cliente();
        cliente.setNome(this.nome());
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
                cliente.getCpfCnpj(),
                cliente.getTelefone(),
                cliente.getEmail()
        );
    }
}