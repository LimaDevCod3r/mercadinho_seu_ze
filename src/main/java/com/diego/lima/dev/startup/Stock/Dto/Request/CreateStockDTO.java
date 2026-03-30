package com.diego.lima.dev.startup.Stock.Dto.Request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateStockDTO(
        @NotNull(message = "O ID do produto é obrigatório")
        Long productId,

        @NotNull(message = "A quantidade é obrigatória")
        @Min(value = 0, message = "A quantidade não pode ser negativa")
        Integer quantity
) {

}
