package com.sistemajuridico.backend.presentation.dtos;

import com.sistemajuridico.backend.core.domain.Escritorio;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EscritorioDTO(
        @NotBlank(message = "A razão social é obrigatória")
        String razaoSocial,

        String nomeFantasia,

        String cnpj,

        String registroOabSociedade,

        String telefone,

        String whatsapp,

        @NotBlank(message = "O e-mail é obrigatório")
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
    public static EscritorioDTO fromEntity(Escritorio escritorio) {
        return new EscritorioDTO(
                escritorio.getRazaoSocial(),
                escritorio.getNomeFantasia(),
                escritorio.getCnpj(),
                escritorio.getRegistroOabSociedade(),
                escritorio.getTelefone(),
                escritorio.getWhatsapp(),
                escritorio.getEmail(),
                escritorio.getCep(),
                escritorio.getLogradouro(),
                escritorio.getNumero(),
                escritorio.getComplemento(),
                escritorio.getBairro(),
                escritorio.getCidade(),
                escritorio.getUf()
        );
    }
}
