package com.diego.lima.dev.startup.Product.Service;

import com.diego.lima.dev.startup.Category.Model.Category;
import com.diego.lima.dev.startup.Category.Repository.CategoryRepository;
import com.diego.lima.dev.startup.Exceptions.Product.NotFoundProductException;
import com.diego.lima.dev.startup.Exceptions.Product.ConflictProductException;
import com.diego.lima.dev.startup.Exceptions.Category.NotFoundCategoryException;
import com.diego.lima.dev.startup.Product.Dtos.Request.CreateProductDTO;
import com.diego.lima.dev.startup.Product.Dtos.Request.UpdateProductDTO;
import com.diego.lima.dev.startup.Product.Dtos.Response.ProductResponse;
import com.diego.lima.dev.startup.Product.Model.Product;
import com.diego.lima.dev.startup.Product.Repository.ProductRepository;
import com.diego.lima.dev.startup.Stock.Model.Stock;
import com.diego.lima.dev.startup.Stock.Repository.StockRepository;
import com.diego.lima.dev.startup.StockMovement.Model.StockMovement;
import com.diego.lima.dev.startup.StockMovement.Repository.StockMovementRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

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

            var response = productService.createProduct(dto);

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
                    () -> productService.createProduct(dto)
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
                    () -> productService.createProduct(dto)
            );

            assertTrue(exception.getMessage().contains("Categoria com id 1 não encontrada"));

            verify(productRepository, never()).save(any(Product.class));
        }
    }

    @Nested
    class DeleteProductTests {

        @Test
        void shouldDeleteProductWithStockAndMovements() {
            var product = new Product();
            product.setId(1L);
            product.setName("Coca-cola");

            var movement = new StockMovement();
            movement.setId(1L);
            movement.setProduct(product);

            var stock = new Stock();
            stock.setId(1L);
            stock.setProduct(product);

            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(stockMovementRepository.findByProduct(product)).thenReturn(List.of(movement));
            when(stockRepository.findByProduct(product)).thenReturn(Optional.of(stock));

            productService.deleteProduct(1L);

            verify(stockMovementRepository, times(1)).deleteAll(List.of(movement));
            verify(stockRepository, times(1)).delete(stock);
            verify(productRepository, times(1)).delete(product);
        }

        @Test
        void shouldDeleteProductWithoutMovements() {
            var product = new Product();
            product.setId(1L);
            product.setName("Coca-cola");

            var stock = new Stock();
            stock.setId(1L);
            stock.setProduct(product);

            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(stockMovementRepository.findByProduct(product)).thenReturn(List.of());
            when(stockRepository.findByProduct(product)).thenReturn(Optional.of(stock));

            productService.deleteProduct(1L);

            verify(stockMovementRepository, never()).deleteAll(anyList());
            verify(stockRepository, times(1)).delete(stock);
            verify(productRepository, times(1)).delete(product);
        }

        @Test
        void shouldDeleteProductWithoutStock() {
            var product = new Product();
            product.setId(1L);
            product.setName("Coca-cola");

            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(stockMovementRepository.findByProduct(product)).thenReturn(List.of());
            when(stockRepository.findByProduct(product)).thenReturn(Optional.empty());

            productService.deleteProduct(1L);

            verify(stockMovementRepository, never()).deleteAll(anyList());
            verify(stockRepository, never()).delete(any());
            verify(productRepository, times(1)).delete(product);
        }

        @Test
        void shouldThrowNotFoundProductExceptionWhenDeletingNonExistentProduct() {
            when(productRepository.findById(1L)).thenReturn(Optional.empty());

            NotFoundProductException exception = assertThrows(
                    NotFoundProductException.class,
                    () -> productService.deleteProduct(1L)
            );

            assertEquals("Produto com id 1 não encontrado", exception.getMessage());

            verify(stockRepository, never()).delete(any());
            verify(stockMovementRepository, never()).deleteAll(anyList());
            verify(productRepository, never()).delete(any());
        }
    }

    @Nested
    class FindAllProductsTests {

        @Test
        void shouldReturnPaginatedProducts() {
            var category = new Category(1L, "Bebidas");
            var pageable = PageRequest.of(0, 5);

            Product p1 = new Product();
            p1.setId(1L);
            p1.setName("Coca-cola");
            p1.setSalePrice(BigDecimal.valueOf(5.50));
            p1.setCategory(category);

            Product p2 = new Product();
            p2.setId(2L);
            p2.setName("Guarana");
            p2.setSalePrice(BigDecimal.valueOf(4.00));
            p2.setCategory(category);

            when(productRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(p1, p2)));

            Page<ProductResponse> result = productService.findAllProducts(pageable);

            assertNotNull(result);
            assertEquals(2, result.getContent().size());
            assertEquals("Coca-cola", result.getContent().get(0).name());
            assertEquals("Bebidas", result.getContent().get(0).category());
            assertEquals(1L, result.getContent().get(0).categoryId());
            verify(productRepository, times(1)).findAll(pageable);
        }

        @Test
        void shouldReturnEmptyPageWhenNoProducts() {
            var pageable = PageRequest.of(0, 5);
            when(productRepository.findAll(pageable)).thenReturn(Page.empty());

            Page<ProductResponse> result = productService.findAllProducts(pageable);

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(productRepository, times(1)).findAll(pageable);
        }
    }

    @Nested
    class FindProductByIdTests {

        @Test
        void shouldReturnProductWhenExists() {
            var category = new Category(1L, "Bebidas");
            var product = new Product();
            product.setId(1L);
            product.setName("Coca-cola");
            product.setSalePrice(BigDecimal.valueOf(8.50));
            product.setCategory(category);

            when(productRepository.findById(1L)).thenReturn(Optional.of(product));

            ProductResponse result = productService.findProductById(1L);

            assertNotNull(result);
            assertEquals(1L, result.id());
            assertEquals("Coca-cola", result.name());
            assertEquals(BigDecimal.valueOf(8.50), result.salePrice());
            assertEquals("Bebidas", result.category());
            assertEquals(1L, result.categoryId());
        }

        @Test
        void shouldThrowNotFoundProductExceptionWhenProductDoesNotExist() {
            Long productId = 99L;
            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            NotFoundProductException exception = assertThrows(
                    NotFoundProductException.class,
                    () -> productService.findProductById(productId)
            );

            assertEquals("Produto com id 99 não encontrado", exception.getMessage());
        }
    }

    @Nested
    class UpdateProductTests {

        @Test
        void shouldUpdateOnlyName() {
            var category = new Category(1L, "Bebidas");
            var product = new Product();
            product.setId(1L);
            product.setName("Coca-cola");
            product.setSalePrice(BigDecimal.valueOf(8.50));
            product.setCategory(category);

            var request = new UpdateProductDTO("Coca-Cola 2L", null, null);

            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(productRepository.existsByNameAndIdNot("Coca-Cola 2L", 1L)).thenReturn(false);

            productService.updateProduct(1L, request);

            assertEquals("Coca-Cola 2L", product.getName());
            assertEquals(BigDecimal.valueOf(8.50), product.getSalePrice());
            assertEquals(1L, product.getCategory().getId());
            verify(productRepository, times(1)).save(product);
        }

        @Test
        void shouldUpdateOnlySalePrice() {
            var category = new Category(1L, "Bebidas");
            var product = new Product();
            product.setId(1L);
            product.setName("Coca-cola");
            product.setSalePrice(BigDecimal.valueOf(8.50));
            product.setCategory(category);

            var request = new UpdateProductDTO(null, BigDecimal.valueOf(10.00), null);

            when(productRepository.findById(1L)).thenReturn(Optional.of(product));

            productService.updateProduct(1L, request);

            assertEquals(BigDecimal.valueOf(10.00), product.getSalePrice());
            assertEquals("Coca-cola", product.getName());
            verify(productRepository, times(1)).save(product);
        }

        @Test
        void shouldUpdateOnlyCategory() {
            var oldCategory = new Category(1L, "Bebidas");
            var newCategory = new Category(2L, "Alimentos");
            var product = new Product();
            product.setId(1L);
            product.setName("Coca-cola");
            product.setSalePrice(BigDecimal.valueOf(8.50));
            product.setCategory(oldCategory);

            var request = new UpdateProductDTO(null, null, 2L);

            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(categoryRepository.findById(2L)).thenReturn(Optional.of(newCategory));

            productService.updateProduct(1L, request);

            assertEquals(2L, product.getCategory().getId());
            assertEquals("Alimentos", product.getCategory().getName());
            verify(categoryRepository, times(1)).findById(2L);
        }

        @Test
        void shouldUpdateAllFields() {
            var oldCategory = new Category(1L, "Bebidas");
            var newCategory = new Category(2L, "Alimentos");
            var product = new Product();
            product.setId(1L);
            product.setName("Coca-cola");
            product.setSalePrice(BigDecimal.valueOf(8.50));
            product.setCategory(oldCategory);

            var request = new UpdateProductDTO("Coca-Cola Zero", BigDecimal.valueOf(7.00), 2L);

            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(productRepository.existsByNameAndIdNot("Coca-Cola Zero", 1L)).thenReturn(false);
            when(categoryRepository.findById(2L)).thenReturn(Optional.of(newCategory));

            productService.updateProduct(1L, request);

            assertEquals("Coca-Cola Zero", product.getName());
            assertEquals(BigDecimal.valueOf(7.00), product.getSalePrice());
            assertEquals(2L, product.getCategory().getId());
        }

        @Test
        void shouldThrowNotFoundProductExceptionWhenUpdatingNonExistentProduct() {
            when(productRepository.findById(1L)).thenReturn(Optional.empty());
            var request = new UpdateProductDTO("Test", BigDecimal.valueOf(1.00), 1L);

            NotFoundProductException exception = assertThrows(
                    NotFoundProductException.class,
                    () -> productService.updateProduct(1L, request)
            );

            assertEquals("Produto com id 1 não encontrado", exception.getMessage());
            verify(productRepository, never()).save(any());
        }

        @Test
        void shouldThrowConflictProductExceptionWhenNameAlreadyExists() {
            var category = new Category(1L, "Bebidas");
            var product = new Product();
            product.setId(1L);
            product.setName("Coca-cola");
            product.setSalePrice(BigDecimal.valueOf(8.50));
            product.setCategory(category);

            var request = new UpdateProductDTO("Guarana", null, null);

            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(productRepository.existsByNameAndIdNot("Guarana", 1L)).thenReturn(true);

            ConflictProductException exception = assertThrows(
                    ConflictProductException.class,
                    () -> productService.updateProduct(1L, request)
            );

            assertEquals("Já existe um produto com esse nome", exception.getMessage());
            verify(productRepository, never()).save(any());
        }

        @Test
        void shouldThrowNotFoundCategoryExceptionWhenNewCategoryDoesNotExist() {
            var category = new Category(1L, "Bebidas");
            var product = new Product();
            product.setId(1L);
            product.setName("Coca-cola");
            product.setSalePrice(BigDecimal.valueOf(8.50));
            product.setCategory(category);

            var request = new UpdateProductDTO(null, null, 99L);

            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

            NotFoundCategoryException exception = assertThrows(
                    NotFoundCategoryException.class,
                    () -> productService.updateProduct(1L, request)
            );

            assertTrue(exception.getMessage().contains("Categoria com id 99 não encontrada"));
            verify(productRepository, never()).save(any());
        }
    }
}
