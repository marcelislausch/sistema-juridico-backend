package com.sistemajuridico.backend.presentation.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sistemajuridico.backend.core.domain.Processo;
import com.sistemajuridico.backend.core.domain.enums.FaseProcessualEnum;
import com.sistemajuridico.backend.core.domain.enums.PapelClienteEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ProcessoDTO(
        UUID id,

        @NotBlank(message = "O número CNJ é obrigatório")
        String numeroCnj,

        String assunto,

        FaseProcessualEnum faseAtual,

        String parteAdversa,

        String cpfCnpjParteAdversa,

        PapelClienteEnum papelCliente,

        BigDecimal valorCausa,

        String comarca,

        LocalDate dataCriacao,

        @NotNull(message = "O ID do cliente é obrigatório")
        @JsonAlias({"clienteId"})
        ClienteResumoDTO cliente,

        @JsonAlias({"advogadoId"})
        UsuarioResumoDTO advogado
) {

    public ProcessoDTO(
            UUID id,
            String numeroCnj,
            String assunto,
            FaseProcessualEnum faseAtual,
            String parteAdversa,
            String cpfCnpjParteAdversa,
            PapelClienteEnum papelCliente,
            BigDecimal valorCausa,
            String comarca,
            LocalDate dataCriacao,
            UUID clienteId,
            UUID advogadoId
    ) {
        this(
                id,
                numeroCnj,
                assunto,
                faseAtual,
                parteAdversa,
                cpfCnpjParteAdversa,
                papelCliente,
                valorCausa,
                comarca,
                dataCriacao,
                clienteId != null ? new ClienteResumoDTO(clienteId) : null,
                advogadoId != null ? new UsuarioResumoDTO(advogadoId) : null
        );
    }

    @JsonIgnore
    public UUID clienteId() {
        return this.cliente != null ? this.cliente.id() : null;
    }

    @JsonIgnore
    public UUID advogadoId() {
        return this.advogado != null ? this.advogado.id() : null;
    }

    public Processo toEntity() {
        Processo processo = new Processo();
        processo.setId(this.id());
        processo.setNumeroCnj(this.numeroCnj());
        processo.setAssunto(this.assunto());
        processo.setFaseAtual(this.faseAtual());
        processo.setParteAdversa(this.parteAdversa());
        processo.setCpfCnpjParteAdversa(this.cpfCnpjParteAdversa());
        processo.setPapelCliente(this.papelCliente());
        processo.setValorCausa(this.valorCausa());
        processo.setComarca(this.comarca());
        processo.setDataCriacao(this.dataCriacao());
        return processo;
    }

    public static ProcessoDTO fromEntity(Processo processo) {
        if (processo == null) {
            return null;
        }

        ClienteResumoDTO clienteResumo = null;
        if (processo.getCliente() != null) {
            clienteResumo = ClienteResumoDTO.fromEntity(processo.getCliente());
        }

        UsuarioResumoDTO advogadoResumo = null;
        if (processo.getAdvogado() != null) {
            advogadoResumo = UsuarioResumoDTO.fromEntity(processo.getAdvogado());
        }

        return new ProcessoDTO(
                processo.getId(),
                processo.getNumeroCnj(),
                processo.getAssunto(),
                processo.getFaseAtual(),
                processo.getParteAdversa(),
                processo.getCpfCnpjParteAdversa(),
                processo.getPapelCliente(),
                processo.getValorCausa(),
                processo.getComarca(),
                processo.getDataCriacao(),
                clienteResumo,
                advogadoResumo
        );
    }
}
