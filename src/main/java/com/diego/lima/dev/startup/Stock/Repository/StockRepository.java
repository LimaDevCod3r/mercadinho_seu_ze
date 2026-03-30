package com.diego.lima.dev.startup.Stock.Repository;

import com.diego.lima.dev.startup.Product.Model.Product;
import com.diego.lima.dev.startup.Stock.Model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {
    boolean existsByProductId(Long productId);
    Optional<Stock> findByProductId(Long productId);
    Optional<Stock> findByProduct(Product product);
}
