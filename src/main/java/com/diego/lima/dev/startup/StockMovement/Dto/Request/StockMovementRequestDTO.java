package com.diego.lima.dev.startup.StockMovement.Dto.Request;

import com.diego.lima.dev.startup.StockMovement.Model.MovementType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record StockMovementRequestDTO(

        @NotNull
        Long productId,

        @NotNull
        @Positive
        Integer quantity,

        @NotNull
        MovementType type,

        String reason
) {
}
