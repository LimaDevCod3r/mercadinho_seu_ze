package com.diego.lima.dev.startup.Category.Dtos.Response;

import com.diego.lima.dev.startup.Product.Dtos.Response.ProductResponse;

import java.util.List;

public record CategoryListResponse(
        Long id,
        String name,
        List<ProductResponse> products
) {
}
