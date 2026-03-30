package com.diego.lima.dev.startup.Sale.Dto.Request;

import com.diego.lima.dev.startup.SalesItems.Dto.Request.SaleItemRequestDTO;

import java.math.BigDecimal;
import java.util.List;

public record SaleRequestDTO(BigDecimal amountPaid, List<SaleItemRequestDTO> items) {
}
