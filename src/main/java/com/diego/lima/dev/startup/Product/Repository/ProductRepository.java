package com.diego.lima.dev.startup.Product.Repository;

import com.diego.lima.dev.startup.Product.Model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByName(String name);
}
