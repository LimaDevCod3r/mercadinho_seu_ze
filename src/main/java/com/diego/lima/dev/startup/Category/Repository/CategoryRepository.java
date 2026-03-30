package com.diego.lima.dev.startup.Category.Repository;

import com.diego.lima.dev.startup.Category.Model.Category;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name);

    @NullMarked
    Optional<Category> findById(Long id);

    boolean existsByNameAndIdNot(String name, Long id);
}

