package com.sistemajuridico.backend.core.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "Dossiê estruturado preparatório para audiência gerado por Inteligência Artificial")
public record ResumoAudienciaEstruturadoDTO(
        @Schema(description = "Fatos já pacificados, incontroversos ou admitidos nos autos",
                example = "[\"Vínculo empregatício formalizado no período de 01/02/2022 a 15/03/2024\", \"Demissão imotivada\"]")
        List<String> fatosIncontroversos,

        @Schema(description = "Pontos de conflito fático que dependem de instrução probatória ou prova oral",
                example = "[\"Cumprimento de horas extras habituais além da jornada contratual\", \"Existência de dano moral decorrente de assédio\"]")
        List<String> fatosControvertidos,

        @Schema(description = "Preliminares processuais, fragilidades da tese e riscos de sucumbência",
                example = "[\"Risco de prescrição bienal ou quinquenal\", \"Fragilidade na prova documental de jornada\"]")
        List<String> riscosProcessuais,

        @Schema(description = "Roteiro de perguntas sugeridas para inquirição de testemunhas ou depoimento da parte contrária",
                example = "[\"Indagar a testemunha se havia controle formal de ponto e se as horas extras eram registradas fielmente\"]")
        List<String> roteiroPerguntas,

        @Schema(description = "Parâmetros, diretrizes e estimativas táticas para formulação de propostas de acordo",
                example = "Recomenda-se proposta conciliatória de até R$ 8.000,00 parcelado em 3x, considerando o risco probatório das horas extras")
        String parametrosAcordo
) {
    public ResumoAudienciaEstruturadoDTO {
        if (fatosIncontroversos == null) {
            fatosIncontroversos = new ArrayList<>();
        }
        if (fatosControvertidos == null) {
            fatosControvertidos = new ArrayList<>();
        }
        if (riscosProcessuais == null) {
            riscosProcessuais = new ArrayList<>();
        }
        if (roteiroPerguntas == null) {
            roteiroPerguntas = new ArrayList<>();
        }
        if (parametrosAcordo == null) {
            parametrosAcordo = "";
        }
    }
}
