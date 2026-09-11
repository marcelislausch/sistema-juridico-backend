package com.sistemajuridico.backend.presentation.dtos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.sistemajuridico.backend.core.domain.Cliente;
import com.sistemajuridico.backend.core.domain.enums.TipoClienteEnum;

import java.util.UUID;

public record ClienteResumoDTO(
        UUID id,
        String nome,
        String cpfCnpj,
        TipoClienteEnum tipo
) {

    public ClienteResumoDTO(UUID id) {
        this(id, null, null, null);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ClienteResumoDTO fromId(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        return new ClienteResumoDTO(UUID.fromString(id));
    }

    public static ClienteResumoDTO fromEntity(Cliente cliente) {
        if (cliente == null) {
            return null;
        }
        return new ClienteResumoDTO(
                cliente.getId(),
                cliente.getNome(),
                cliente.getCpfCnpj(),
                cliente.getTipo()
        );
    }
}
