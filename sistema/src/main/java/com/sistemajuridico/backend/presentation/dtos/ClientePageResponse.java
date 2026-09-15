package com.sistemajuridico.backend.presentation.dtos;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

@Schema(name = "ClientePageResponse")
public record ClientePageResponse(
    @ArraySchema(
        schema = @Schema(implementation = ClienteDTO.class),
        arraySchema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    )
    List<ClienteDTO> content,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    PageMetadataDTO page
) {
    public static ClientePageResponse from(Page<ClienteDTO> result) {
        List<ClienteDTO> content = new ArrayList<>();
        if (result != null && result.getContent() != null) {
            for (ClienteDTO item : result.getContent()) {
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
        return new ClientePageResponse(content, metadata);
    }
}
