package com.diego.lima.dev.startup.Category.Service;

import com.diego.lima.dev.startup.Category.Dtos.Request.CreateCategoryDTO;
import com.diego.lima.dev.startup.Category.Dtos.Request.UpdateCategoryDTO;
import com.diego.lima.dev.startup.Category.Dtos.Response.CategoryResponse;
import com.diego.lima.dev.startup.Category.Model.Category;
import com.diego.lima.dev.startup.Category.Repository.CategoryRepository;
import com.diego.lima.dev.startup.Exceptions.EntityConflictException;
import com.diego.lima.dev.startup.Exceptions.EntityNotFoundException;
import com.diego.lima.dev.startup.Product.Dtos.Response.ProductResponse;
import com.diego.lima.dev.startup.Product.Repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryService(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    public CategoryResponse createCategory(CreateCategoryDTO request) {

        if (categoryRepository.existsByName(request.name())) {
            throw new EntityConflictException(String.format("Categoria: %s já existe", request.name()));
        }

        var categoryEntity = new Category();
        categoryEntity.setName(request.name());

        Category savedCategoryEntity = categoryRepository.save(categoryEntity);

        return new CategoryResponse(savedCategoryEntity.getId(), savedCategoryEntity.getName());
    }

    public List<CategoryResponse> findAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(category -> new CategoryResponse(category.getId(), category.getName()))
                .toList();
    }

    public Page<ProductResponse> findProductsByCategory(Long categoryId, Pageable pageable) {
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("A categoria do ID: %s não foi encontrada.", categoryId)
                ));

        return productRepository.findByCategoryId(categoryId, pageable)
                .map(product -> new ProductResponse(
                        product.getId(),
                        product.getName(),
                        product.getSalePrice(),
                        product.getCategory().getName(),
                        product.getCategory().getId()
                ));
    }

    public void updateCategory(Long categoryId, UpdateCategoryDTO request) {

        var categoryEntity = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                String.format("A categoria do ID: %s não foi encontrado.", categoryId)
                        )
                );

        if (request.name() != null && !request.name().isBlank()) {
            String newName = request.name().trim();

            boolean categoryNameExists =
                    categoryRepository.existsByNameAndIdNot(newName, categoryId);

            if (categoryNameExists) {
                throw new EntityConflictException(
                        String.format("Categoria com nome %s já existe", newName)
                );
            }

            categoryEntity.setName(request.name());
        }

        categoryRepository.save(categoryEntity);
    }

    public void deleteCategory(Long categoryId) {

        var category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                String.format("A categoria do ID: %s não foi encontrado.", categoryId)
                        )
                );

        categoryRepository.deleteById(category.getId());
    }
}
