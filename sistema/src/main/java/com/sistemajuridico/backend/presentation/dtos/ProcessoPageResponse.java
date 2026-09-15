package com.sistemajuridico.backend.presentation.dtos;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

@Schema(name = "ProcessoPageResponse")
public record ProcessoPageResponse(
    @ArraySchema(
        schema = @Schema(implementation = ProcessoDTO.class),
        arraySchema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    )
    List<ProcessoDTO> content,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    PageMetadataDTO page
) {
    public static ProcessoPageResponse from(Page<ProcessoDTO> result) {
        List<ProcessoDTO> content = new ArrayList<>();
        if (result != null && result.getContent() != null) {
            for (ProcessoDTO item : result.getContent()) {
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
        return new ProcessoPageResponse(content, metadata);
    }
}
