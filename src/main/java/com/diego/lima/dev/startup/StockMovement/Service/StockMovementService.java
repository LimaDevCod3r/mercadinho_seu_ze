package com.diego.lima.dev.startup.StockMovement.Service;

import com.diego.lima.dev.startup.Exceptions.Product.NotFoundProductException;
import com.diego.lima.dev.startup.Exceptions.Stock.ConflictStockException;
import com.diego.lima.dev.startup.Exceptions.Stock.NotFoundStockException;
import com.diego.lima.dev.startup.Product.Repository.ProductRepository;
import com.diego.lima.dev.startup.Stock.Repository.StockRepository;
import org.springframework.transaction.annotation.Transactional;
import com.diego.lima.dev.startup.StockMovement.Dto.Request.StockMovementRequestDTO;
import com.diego.lima.dev.startup.StockMovement.Dto.Response.StockMovementResponseDTO;
import com.diego.lima.dev.startup.StockMovement.Model.StockMovement;
import com.diego.lima.dev.startup.StockMovement.Repository.StockMovementRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class StockMovementService {
    private final ProductRepository productRepository;
    private final StockRepository stockRepository;
    private final StockMovementRepository stockMovementRepository;


    public StockMovementService(ProductRepository productRepository, StockRepository stockRepository, StockMovementRepository stockMovementRepository) {
        this.productRepository = productRepository;
        this.stockRepository = stockRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    @Transactional
    public StockMovementResponseDTO createMovement(StockMovementRequestDTO request) {

        var productEntity = productRepository.findById(request.productId())
                .orElseThrow(() ->
                        new NotFoundProductException(String.format("Produto com id %s não encontrado", request.productId()))
                );

        var stockEntity = stockRepository.findByProduct(productEntity).orElseThrow(() ->
                new NotFoundStockException(
                        String.format("Estoque do produto %s não encontrado", productEntity.getId())
                )
        );

        switch (request.type()) {
            case ENTRY -> stockEntity.setQuantity(stockEntity.getQuantity() + request.quantity());
            case EXIT, LOSS -> {
                if (stockEntity.getQuantity() < request.quantity()) {
                    throw new ConflictStockException(
                            String.format("Estoque insuficiente. Disponível: %d, solicitado: %d",
                                    stockEntity.getQuantity(),
                                    request.quantity()
                            )
                    );
                }
                stockEntity.setQuantity(stockEntity.getQuantity() - request.quantity());
            }
            case ADJUSTMENT -> stockEntity.setQuantity(request.quantity());
        }
        stockRepository.save(stockEntity);

        var stockMovementEntity = new StockMovement();
        stockMovementEntity.setProduct(productEntity);
        stockMovementEntity.setQuantity(request.quantity());
        stockMovementEntity.setType(request.type());
        stockMovementEntity.setReason(request.reason());

        stockMovementRepository.save(stockMovementEntity);

        return new StockMovementResponseDTO(
                stockMovementEntity.getId(),
                stockMovementEntity.getQuantity(),
                stockMovementEntity.getType(),
                stockMovementEntity.getReason()
        );
    }

    public Page<StockMovementResponseDTO> findAllStockMovement(Pageable pageable) {
        Page<StockMovement> stockMovementPage = stockMovementRepository.findAll(pageable);

        return stockMovementPage.map((entity) ->
                new StockMovementResponseDTO(
                        entity.getId(),
                        entity.getQuantity(),
                        entity.getType(),
                        entity.getReason())
        );
    }
}
