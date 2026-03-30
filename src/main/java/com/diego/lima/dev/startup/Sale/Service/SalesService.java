package com.diego.lima.dev.startup.Sale.Service;

import com.diego.lima.dev.startup.Product.Repository.ProductRepository;
import com.diego.lima.dev.startup.Sale.Dto.Request.SaleRequestDTO;
import com.diego.lima.dev.startup.Sale.Dto.Response.SaleResponseDTO;
import com.diego.lima.dev.startup.Sale.Model.Sale;
import com.diego.lima.dev.startup.Sale.Repository.SalesRepository;
import com.diego.lima.dev.startup.SalesItems.Dto.Request.SaleItemRequestDTO;
import com.diego.lima.dev.startup.SalesItems.Dto.Response.SaleItemResponseDTO;
import com.diego.lima.dev.startup.SalesItems.Model.SaleItem;
import com.diego.lima.dev.startup.SalesItems.Repository.SaleItemRepository;
import com.diego.lima.dev.startup.Stock.Repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SalesService {

    private final SalesRepository salesRepository;
    private final ProductRepository productRepository;
    private final SaleItemRepository saleItemRepository;
    private final StockRepository stockRepository;

    public SalesService(SalesRepository salesRepository, ProductRepository productRepository, SaleItemRepository saleItemRepository, StockRepository stockRepository) {
        this.salesRepository = salesRepository;
        this.productRepository = productRepository;
        this.saleItemRepository = saleItemRepository;
        this.stockRepository  = stockRepository;
    }


    @Transactional
    public SaleResponseDTO createSale(SaleRequestDTO request) {
        Sale sale = new Sale();
        sale.setSaleDate(LocalDateTime.now());
        sale.setTotal(BigDecimal.ZERO);
        sale = salesRepository.save(sale);

        BigDecimal total = BigDecimal.ZERO;
        List<SaleItemResponseDTO> itemsResponse = new ArrayList<>();

        for (SaleItemRequestDTO itemDTO : request.items()) {
            var product = productRepository.findById(itemDTO.productId())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado: " + itemDTO.productId()));

            // ✅ Busca o estoque do produto
            var stock = stockRepository.findByProduct(product)
                    .orElseThrow(() -> new RuntimeException("Estoque não encontrado para: " + product.getName()));

            // ✅ Valida se tem quantidade suficiente
            if (stock.getQuantity() < itemDTO.quantity()) {
                throw new RuntimeException(
                        "Estoque insuficiente para: " + product.getName() +
                                " | Disponível: " + stock.getQuantity() +
                                " | Solicitado: " + itemDTO.quantity()
                );
            }

            BigDecimal unitPrice = product.getSalePrice();
            BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(itemDTO.quantity()));

            SaleItem saleItem = new SaleItem();
            saleItem.setSale(sale);
            saleItem.setProduct(product);
            saleItem.setQuantity(itemDTO.quantity());
            saleItem.setUnitPrice(unitPrice);
            saleItem.setTotalPrice(totalPrice);
            saleItemRepository.save(saleItem);

            // ✅ Desconta do estoque
            stock.setQuantity(stock.getQuantity() - itemDTO.quantity());
            stockRepository.save(stock);

            total = total.add(totalPrice);

            itemsResponse.add(new SaleItemResponseDTO(
                    product.getName(),
                    itemDTO.quantity(),
                    unitPrice,
                    totalPrice
            ));
        }

        sale.setTotal(total);
        salesRepository.save(sale);

        BigDecimal amountPaid = request.amountPaid();
        if (amountPaid.compareTo(total) < 0) {
            throw new RuntimeException(
                    "Valor pago insuficiente. Total: R$" + total + " | Pago: R$" + amountPaid
            );
        }
        BigDecimal change = amountPaid.subtract(total);

        return new SaleResponseDTO(
                sale.getId(),
                sale.getSaleDate(),
                itemsResponse,
                total,
                amountPaid,
                change
        );
    }

}
