package com.diego.lima.dev.startup.Category.Controller;

import com.diego.lima.dev.startup.Category.Dtos.Request.CreateCategoryDTO;
import com.diego.lima.dev.startup.Category.Dtos.Request.UpdateCategoryDTO;
import com.diego.lima.dev.startup.Category.Dtos.Response.CategoryResponse;
import com.diego.lima.dev.startup.Category.Service.CategoryService;
import com.diego.lima.dev.startup.Product.Dtos.Response.ProductResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CreateCategoryDTO request) {
        var response = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> findAllCategories() {
        var response = categoryService.findAllCategories();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/products")
    public ResponseEntity<Page<ProductResponse>> findProductsByCategoryId(
            @PathVariable Long id,
            Pageable pageable
    ) {
        var response = categoryService.findProductsByCategory(id, pageable);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateCategoryById(@PathVariable Long id, @RequestBody UpdateCategoryDTO request) {
        categoryService.updateCategory(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategoryById(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
