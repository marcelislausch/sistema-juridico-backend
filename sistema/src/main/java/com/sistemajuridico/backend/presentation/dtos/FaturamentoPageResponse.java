package com.sistemajuridico.backend.presentation.dtos;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

@Schema(name = "FaturamentoPageResponse")
public record FaturamentoPageResponse(
    @ArraySchema(
        schema = @Schema(implementation = FaturamentoDTO.class),
        arraySchema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    )
    List<FaturamentoDTO> content,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    PageMetadataDTO page
) {
    public static FaturamentoPageResponse from(Page<FaturamentoDTO> result) {
        List<FaturamentoDTO> content = new ArrayList<>();
        if (result != null && result.getContent() != null) {
            for (FaturamentoDTO item : result.getContent()) {
                content.add(item);
            }
        }

        int size = 0;
        int number = 0;
        long totalElements = 0L;
        int totalPages = 0;

        if (result != null) {
            size = result.getSize();
            number = result.getNumber();
            totalElements = result.getTotalElements();
            totalPages = result.getTotalPages();
        }

        PageMetadataDTO metadata = new PageMetadataDTO(size, number, totalElements, totalPages);
        return new FaturamentoPageResponse(content, metadata);
    }
}
