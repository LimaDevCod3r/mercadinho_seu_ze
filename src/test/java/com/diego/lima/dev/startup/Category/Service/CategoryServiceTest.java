package com.diego.lima.dev.startup.Category.Service;

import com.diego.lima.dev.startup.Category.Dtos.Request.CreateCategoryDTO;
import com.diego.lima.dev.startup.Category.Dtos.Request.UpdateCategoryDTO;
import com.diego.lima.dev.startup.Category.Dtos.Response.CategoryResponse;
import com.diego.lima.dev.startup.Category.Model.Category;
import com.diego.lima.dev.startup.Category.Repository.CategoryRepository;
import com.diego.lima.dev.startup.Exceptions.EntityConflictException;
import com.diego.lima.dev.startup.Exceptions.EntityNotFoundException;
import com.diego.lima.dev.startup.Product.Dtos.Response.ProductResponse;
import com.diego.lima.dev.startup.Product.Model.Product;
import com.diego.lima.dev.startup.Product.Repository.ProductRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CategoryService service;

    public CategoryServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    class CreateCategoryTests {

        @Test
        void shouldCreateCategory() {
            CreateCategoryDTO dto = new CreateCategoryDTO("Bebidas");

            when(categoryRepository.existsByName("Bebidas")).thenReturn(false);

            Category savedCategory = new Category();
            savedCategory.setName("Bebidas");

            when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

            CategoryResponse result = service.createCategory(dto);

            assertNotNull(result);
            assertEquals("Bebidas", result.name());

            verify(categoryRepository, times(1)).save(any(Category.class));
        }

        @Test
        void shouldNotBePossibleCreateCategoryWithSameName() {
            CreateCategoryDTO dto = new CreateCategoryDTO("Bebidas");

            when(categoryRepository.existsByName("Bebidas")).thenReturn(true);

            EntityConflictException exception = assertThrows(
                    EntityConflictException.class,
                    () -> service.createCategory(dto)
            );

            assertEquals(
                    "Categoria: Bebidas já existe",
                    exception.getMessage()
            );

            verify(categoryRepository, never()).save(any(Category.class));
        }
    }

    @Nested
    class FindAllCategoryTest {

        @Test
        void shouldReturnAllCategories() {
            Category category1 = new Category(1L, "Alimentação");
            Category category2 = new Category(2L, "Bebidas");

            when(categoryRepository.findAll()).thenReturn(List.of(category1, category2));

            List<CategoryResponse> result = service.findAllCategories();

            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("Alimentação", result.get(0).name());
            assertEquals("Bebidas", result.get(1).name());

            verify(categoryRepository, times(1)).findAll();
        }

        @Test
        void shouldReturnEmptyListWhenNoCategories() {
            when(categoryRepository.findAll()).thenReturn(List.of());

            List<CategoryResponse> result = service.findAllCategories();

            assertNotNull(result);
            assertTrue(result.isEmpty());

            verify(categoryRepository, times(1)).findAll();
        }
    }

    @Nested
    class FindProductsByCategoryIdTests {

        @Test
        void shouldReturnPaginatedProductsWhenCategoryExists() {
            Long categoryId = 1L;
            Pageable pageable = PageRequest.of(0, 5);

            Category category = new Category(categoryId, "Bebidas");

            Product product = new Product();
            product.setId(10L);
            product.setName("Coca-Cola");
            product.setSalePrice(new BigDecimal("5.50"));
            product.setCategory(category);

            Page<Product> productPage = new PageImpl<>(List.of(product));

            when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
            when(productRepository.findByCategoryId(categoryId, pageable)).thenReturn(productPage);

            Page<ProductResponse> result = service.findProductsByCategory(categoryId, pageable);

            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            assertEquals("Coca-Cola", result.getContent().get(0).name());
            assertEquals("Bebidas", result.getContent().get(0).category());

            verify(categoryRepository, times(1)).findById(categoryId);
            verify(productRepository, times(1)).findByCategoryId(categoryId, pageable);
        }

        @Test
        void shouldThrowNotFoundExceptionWhenCategoryDoesNotExist() {
            Long categoryId = 99L;
            Pageable pageable = PageRequest.of(0, 5);

            when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> service.findProductsByCategory(categoryId, pageable)
            );

            assertEquals(
                    String.format("A categoria do ID: %s não foi encontrada.", categoryId),
                    exception.getMessage()
            );

            verify(productRepository, never()).findByCategoryId(any(), any());
        }
    }

    @Nested
    class UpdateCategoryById {

        @Test
        void shouldPartialUpdateByIdCategory() {
            Long id = 1L;
            Category category = new Category(id, "Bebidas");
            UpdateCategoryDTO request = new UpdateCategoryDTO("Comidas");

            when(categoryRepository.findById(id)).thenReturn(Optional.of(category));

            service.updateCategory(id, request);

            assertEquals("Comidas", category.getName());
            verify(categoryRepository).save(category);
        }

        @Test
        void shouldThrowEntityNotFoundExceptionWhenIdIsNotFound() {
            Long id = 1L;
            UpdateCategoryDTO request = new UpdateCategoryDTO("Comidas");

            when(categoryRepository.findById(id)).thenReturn(Optional.empty());

            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> service.updateCategory(id, request)
            );

            assertEquals(
                    String.format("A categoria do ID: %s não foi encontrado.", id),
                    exception.getMessage()
            );

            verify(categoryRepository, never()).save(any());
        }
    }

    @Nested
    class DeleteCategoryById {

        @Test
        void shouldDeleteCategoryById() {
            Long id = 1L;
            Category category = new Category(id, "Bebidas");

            when(categoryRepository.findById(id)).thenReturn(Optional.of(category));

            service.deleteCategory(id);

            verify(categoryRepository).deleteById(id);
        }

        @Test
        void shouldThrowNotFoundExceptionWhenToDeleteCategoryByIdNotExist() {
            Long id = 1L;
            when(categoryRepository.findById(id)).thenReturn(Optional.empty());

            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> service.deleteCategory(id)
            );

            assertEquals(
                    String.format("A categoria do ID: %s não foi encontrado.", id),
                    exception.getMessage()
            );

            verify(categoryRepository, never()).deleteById(any());
        }
    }
}
