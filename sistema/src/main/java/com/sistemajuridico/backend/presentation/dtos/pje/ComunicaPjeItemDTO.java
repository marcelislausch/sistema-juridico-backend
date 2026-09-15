package com.sistemajuridico.backend.presentation.dtos.pje;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ComunicaPjeItemDTO(
        Long id,
        @JsonProperty("data_disponibilizacao")
        String dataDisponibilizacao,
        String siglaTribunal,
        String tipoComunicacao,
        String nomeOrgao,
        Integer idOrgao,
        String texto,
        @JsonProperty("numero_processo")
        String numeroProcesso,
        @JsonProperty("numeroprocessocommascara")
        String numeroProcessoComMascara,
        String meio,
        String link,
        String tipoDocumento,
        String nomeClasse,
        String codigoClasse,
        String hash,
        @JsonProperty("destinatarioadvogados")
        List<ComunicaPjeDestinatarioAdvogadoDTO> destinatarioAdvogados
) {
    public ComunicaPjeItemDTO {
        if (destinatarioAdvogados == null) {
            destinatarioAdvogados = new ArrayList<>();
        }
    }

    public LocalDate obterDataDisponibilizacaoAsLocalDate() {
        if (dataDisponibilizacao != null && !dataDisponibilizacao.trim().isEmpty()) {
            try {
                return LocalDate.parse(dataDisponibilizacao.trim());
            } catch (Exception e) {
                return LocalDate.now();
            }
        }
        return LocalDate.now();
    }
}
