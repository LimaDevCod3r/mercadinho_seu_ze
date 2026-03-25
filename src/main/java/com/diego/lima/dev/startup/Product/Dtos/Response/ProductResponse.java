package com.diego.lima.dev.startup.Product.Dtos.Response;

import com.diego.lima.dev.startup.Category.Dtos.Response.CategoryResponse;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        BigDecimal salePrice,
        String category
) {
}
