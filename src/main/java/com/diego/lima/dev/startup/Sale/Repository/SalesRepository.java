package com.diego.lima.dev.startup.Sale.Repository;


import com.diego.lima.dev.startup.Sale.Model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalesRepository extends JpaRepository<Sale, Long> {
}
