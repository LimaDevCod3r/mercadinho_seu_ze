package com.diego.lima.dev.startup.Category.Dtos.Request;

import jakarta.validation.constraints.NotBlank;

public record CreateCategoryDTO(

        @NotBlank(message = "Categória precisa de um 'nome' válido")
        String name
) {
}
