package com.sistemajuridico.backend.presentation.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PageMetadata", description = "Metadados da paginação baseada em zero")
public record PageMetadataDTO(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, minimum = "1", example = "20")
    int size,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, minimum = "0", example = "0")
    int number,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, minimum = "0", example = "125")
    long totalElements,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, minimum = "0", example = "7")
    int totalPages
) {}
