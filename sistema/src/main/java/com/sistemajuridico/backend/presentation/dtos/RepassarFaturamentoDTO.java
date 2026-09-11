package com.sistemajuridico.backend.presentation.dtos;

import java.time.LocalDate;

public record RepassarFaturamentoDTO(
        LocalDate dataRepasse,
        String formaRepasse
) {
}
