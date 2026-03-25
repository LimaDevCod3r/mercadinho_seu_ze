package com.diego.lima.dev.startup.Product.Dtos.Request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateProductDTO(

        @NotBlank(message = "Produto precisa de um nome válido")
        @Size(min = 3, max = 150, message = "Produto precisa ter entre 3 e 150 caracteres")
        String name,

        @NotNull(message = "O preço é obrigatório")
        @DecimalMin(value = "0.01", message = "O preço deve ser maior que zero")
        BigDecimal salePrice,

        @NotNull(message = "A categoria é obrigatória")
        @Positive(message = "O ID da categoria deve ser maior que zero")
        Long categoryId

) {
}
