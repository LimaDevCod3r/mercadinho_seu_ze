package com.diego.lima.dev.startup.SalesItems.Dto.Response;

import java.math.BigDecimal;

public record SaleItemResponseDTO(String productName, Integer quantity, BigDecimal unitPrice, BigDecimal totalPrice) {
}
