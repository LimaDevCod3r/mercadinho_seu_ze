package com.diego.lima.dev.startup.StockMovement.Repository;

import com.diego.lima.dev.startup.Product.Model.Product;
import com.diego.lima.dev.startup.StockMovement.Model.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    List<StockMovement> findByProduct(Product product);
}
