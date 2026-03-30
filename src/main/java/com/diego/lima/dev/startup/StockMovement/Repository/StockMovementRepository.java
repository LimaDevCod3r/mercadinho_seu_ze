package com.diego.lima.dev.startup.StockMovement.Repository;

import com.diego.lima.dev.startup.StockMovement.Model.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
}
