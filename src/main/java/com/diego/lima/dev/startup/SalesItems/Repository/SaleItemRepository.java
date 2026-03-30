package com.diego.lima.dev.startup.SalesItems.Repository;

import com.diego.lima.dev.startup.SalesItems.Model.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {
}
