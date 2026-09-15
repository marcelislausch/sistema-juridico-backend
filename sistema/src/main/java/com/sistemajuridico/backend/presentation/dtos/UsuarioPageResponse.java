package com.sistemajuridico.backend.presentation.dtos;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

@Schema(name = "UsuarioPageResponse")
public record UsuarioPageResponse(
    @ArraySchema(
        schema = @Schema(implementation = UsuarioResponseDTO.class),
        arraySchema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    )
    List<UsuarioResponseDTO> content,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    PageMetadataDTO page
) {
    public static UsuarioPageResponse from(Page<UsuarioResponseDTO> result) {
        List<UsuarioResponseDTO> content = new ArrayList<>();
        if (result != null && result.getContent() != null) {
            for (UsuarioResponseDTO item : result.getContent()) {
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
        return new UsuarioPageResponse(content, metadata);
    }
}
