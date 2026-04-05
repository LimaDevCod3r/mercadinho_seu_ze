package com.diego.lima.dev.startup.Stock.Service;

import com.diego.lima.dev.startup.Exceptions.EntityConflictException;
import com.diego.lima.dev.startup.Exceptions.EntityNotFoundException;
import com.diego.lima.dev.startup.Product.Dtos.Response.ProductResponse;
import com.diego.lima.dev.startup.Product.Repository.ProductRepository;
import com.diego.lima.dev.startup.Stock.Dto.Request.CreateStockDTO;
import com.diego.lima.dev.startup.Stock.Dto.Response.StockResponse;
import com.diego.lima.dev.startup.Stock.Model.Stock;
import com.diego.lima.dev.startup.Stock.Repository.StockRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class StockService {
    private final StockRepository stockRepository;
    private final ProductRepository productRepository;

    public StockService(StockRepository stockRepository, ProductRepository productRepository) {
        this.stockRepository = stockRepository;
        this.productRepository = productRepository;
    }

    public StockResponse createStock(CreateStockDTO request) {

        if (stockRepository.existsByProductId(request.productId())) {
            throw new EntityConflictException("O estoque deste produto já foi criado. Para adicionar mais quantidade, utilize a movimentação de entrada.");
        }

        var productEntity = productRepository.findById(request.productId())
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                String.format("Produto com id %s não foi encontrado", request.productId())
                        )
                );

        var stockEntity = new Stock();
        stockEntity.setQuantity(request.quantity());
        stockEntity.setProduct(productEntity);

        stockEntity = stockRepository.save(stockEntity);

        var product = new ProductResponse(
                stockEntity.getProduct().getId(),
                stockEntity.getProduct().getName(),
                stockEntity.getProduct().getSalePrice(),
                stockEntity.getProduct().getCategory().getName(),
                stockEntity.getProduct().getCategory().getId()
        );

        return new StockResponse(
                stockEntity.getId(),
                product,
                stockEntity.getQuantity()
        );
    }


    public StockResponse findStockById(Long stockId) {

        var stockEntity = stockRepository.findById(stockId)
                .orElseThrow(
                        () -> new EntityNotFoundException(String.format("Estoque do id: %d não encontrado", stockId)));


        var product = new ProductResponse(
                stockEntity.getProduct().getId(),
                stockEntity.getProduct().getName(),
                stockEntity.getProduct().getSalePrice(),
                stockEntity.getProduct().getCategory().getName(),
                stockEntity.getProduct().getCategory().getId()
        );

        return new StockResponse(stockEntity.getId(), product, stockEntity.getQuantity());
    }


    public Page<StockResponse> findAllStocks(Pageable pageable) {
        Page<Stock> stockPage = stockRepository.findAll(pageable);

        return stockPage.map(stockEntity -> {

            var product = new ProductResponse(
                    stockEntity.getProduct().getId(),
                    stockEntity.getProduct().getName(),
                    stockEntity.getProduct().getSalePrice(),
                    stockEntity.getProduct().getCategory().getName(),
                    stockEntity.getProduct().getCategory().getId()
            );

            return new StockResponse(
                    stockEntity.getId(),
                    product,
                    stockEntity.getQuantity()
            );
        });
    }

    public StockResponse findStockByProductId(Long productId) {
        var stockEntity = stockRepository.findByProductId(productId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                String.format("Estoque do produto id: %d não encontrado", productId)
                        )
                );

        var product = new ProductResponse(
                stockEntity.getProduct().getId(),
                stockEntity.getProduct().getName(),
                stockEntity.getProduct().getSalePrice(),
                stockEntity.getProduct().getCategory().getName(),
                stockEntity.getProduct().getCategory().getId()
        );

        return new StockResponse(
                stockEntity.getId(),
                product,
                stockEntity.getQuantity()
        );
    }
}
