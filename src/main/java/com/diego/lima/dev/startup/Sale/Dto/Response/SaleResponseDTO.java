package com.diego.lima.dev.startup.Sale.Dto.Response;

import com.diego.lima.dev.startup.SalesItems.Dto.Response.SaleItemResponseDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record SaleResponseDTO(Long saleId, LocalDateTime saleDate, List<SaleItemResponseDTO> items, BigDecimal total,
                              BigDecimal amountPaid, BigDecimal change) {
}
