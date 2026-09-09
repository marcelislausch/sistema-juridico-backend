package com.sistemajuridico.backend.core.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum DestinoNotificacaoEnum {
    AUDIENCIAS("audiencias"),
    AGENDA("agenda"),
    FINANCEIRO("financeiro");

    private final String rota;

    DestinoNotificacaoEnum(String rota) {
        this.rota = rota;
    }

    @JsonValue
    public String getRota() {
        return rota;
    }
}
