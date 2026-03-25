package com.diego.lima.dev.startup.Category.Service;

import com.diego.lima.dev.startup.Category.Dtos.Request.CreateCategoryDTO;
import com.diego.lima.dev.startup.Category.Dtos.Request.UpdateCategoryDTO;
import com.diego.lima.dev.startup.Category.Dtos.Response.CategoryResponse;
import com.diego.lima.dev.startup.Category.Model.Category;
import com.diego.lima.dev.startup.Category.Repository.CategoryRepository;
import com.diego.lima.dev.startup.Exceptions.Category.ConflictCategoryException;
import com.diego.lima.dev.startup.Exceptions.Category.NotFoundCategoryException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public CategoryResponse create(CreateCategoryDTO request) {


        if (categoryRepository.existsByName(request.name())) {
            throw new ConflictCategoryException(String.format("Categoria: %s já existe", request.name()));
        }

        Category category = new Category();
        category.setName(request.name());

        Category savedCategory = categoryRepository.save(category);

        return new CategoryResponse(savedCategory.getId(), savedCategory.getName());
    }

    public Page<CategoryResponse> findAll(Pageable pageable) {
        Page<Category> categories = categoryRepository.findAll(pageable);

        return categories.map(category ->
                new CategoryResponse(category.getId(), category.getName())
        );
    }

    public void updateById(Long id, UpdateCategoryDTO request) {

        var category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundCategoryException(
                                String.format("A categoria do ID: %s não foi encontrado.", id)
                        )
                );

        if (request.name() != null && !request.name().isBlank()) {
            category.setName(request.name());
        }

        categoryRepository.save(category);
    }

    public void deleteById(Long id) {

        var category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundCategoryException(
                                String.format("A categoria do ID: %s não foi encontrado.", id)
                        )
                );

        categoryRepository.deleteById(category.getId());
    }
}
