package com.diego.lima.dev.startup.StockMovement.Dto.Response;

import com.diego.lima.dev.startup.StockMovement.Model.MovementType;
import com.fasterxml.jackson.annotation.JsonInclude;

public record StockMovementResponseDTO(
        Long id,
        Integer quantity,
        MovementType type,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String reason
) {
}
