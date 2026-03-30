package com.diego.lima.dev.startup.Stock.Dto.Response;

import com.diego.lima.dev.startup.Product.Dtos.Response.ProductResponse;

public record StockResponse(Long id, ProductResponse product, Integer quantity) {
}
