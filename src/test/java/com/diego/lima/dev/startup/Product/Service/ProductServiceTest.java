package com.diego.lima.dev.startup.Product.Service;

import com.diego.lima.dev.startup.Category.Model.Category;
import com.diego.lima.dev.startup.Category.Repository.CategoryRepository;
import com.diego.lima.dev.startup.Exceptions.Category.NotFoundCategoryException;
import com.diego.lima.dev.startup.Exceptions.Product.ConflictProductException;
import com.diego.lima.dev.startup.Product.Dtos.Request.CreateProductDTO;
import com.diego.lima.dev.startup.Product.Model.Product;
import com.diego.lima.dev.startup.Product.Repository.ProductRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    public ProductServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    class CreateProductTests {

        @Test
        void shouldCreateProduct() {

            var category = new Category(1L, "Bebidas");
            CreateProductDTO dto = new CreateProductDTO("Coca-cola", BigDecimal.valueOf(8.50), category.getId());

            when(productRepository.existsByName("Coca-cola")).thenReturn(false);
            when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));

            Product savedProduct = new Product();
            savedProduct.setId(1L);
            savedProduct.setName(dto.name());
            savedProduct.setSalePrice(dto.salePrice());
            savedProduct.setCategory(category);

            when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

            var response = productService.create(dto);

            assertNotNull(response);
            assertEquals("Coca-cola", response.name());
            assertEquals(BigDecimal.valueOf(8.50), response.salePrice());
            assertEquals("Bebidas", response.category());

            verify(productRepository, times(1)).save(any(Product.class));
        }


        @Test
        void shouldReturnConflictProductException() {
            var categoryId = 1L;
            CreateProductDTO dto = new CreateProductDTO("Coca-cola", BigDecimal.valueOf(8.50), categoryId);

            when(productRepository.existsByName("Coca-cola")).thenReturn(true);

            ConflictProductException exception = assertThrows(
                    ConflictProductException.class,
                    () -> productService.create(dto)
            );

            assertEquals("Produto com nome Coca-cola já existe", exception.getMessage());

            verify(productRepository, never()).save(any(Product.class));
            verify(categoryRepository, never()).findById(anyLong());
        }

        @Test
        void shouldReturnNotFoundCategoryException() {
            var categoryId = 1L;
            CreateProductDTO dto = new CreateProductDTO("Coca-cola", BigDecimal.valueOf(8.50), categoryId);

            when(productRepository.existsByName("Coca-cola")).thenReturn(false);
            when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

            NotFoundCategoryException exception = assertThrows(
                    NotFoundCategoryException.class,
                    () -> productService.create(dto)
            );

            assertTrue(exception.getMessage().contains("Categoria com id 1 não encontrada"));

            verify(productRepository, never()).save(any(Product.class));
        }
    }
}
