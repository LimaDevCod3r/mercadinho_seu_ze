package com.diego.lima.dev.startup.Stock.Service;

import com.diego.lima.dev.startup.Category.Model.Category;
import com.diego.lima.dev.startup.Exceptions.Product.NotFoundProductException;
import com.diego.lima.dev.startup.Exceptions.Stock.ConflictStockException;
import com.diego.lima.dev.startup.Exceptions.Stock.NotFoundStockException;
import com.diego.lima.dev.startup.Product.Model.Product;
import com.diego.lima.dev.startup.Product.Repository.ProductRepository;
import com.diego.lima.dev.startup.Stock.Dto.Request.CreateStockDTO;
import com.diego.lima.dev.startup.Stock.Dto.Response.StockResponse;
import com.diego.lima.dev.startup.Stock.Model.Stock;
import com.diego.lima.dev.startup.Stock.Repository.StockRepository;
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
import static org.mockito.Mockito.*;

class StockServiceTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private StockService stockService;

    public StockServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    class CreateStockTests {

        @Test
        void shouldCreateStock() {
            var category = new Category(1L, "Bebidas");
            var product = new Product();
            product.setId(1L);
            product.setName("Coca-cola");
            product.setSalePrice(BigDecimal.valueOf(8.50));
            product.setCategory(category);

            var request = new CreateStockDTO(1L, 100);

            when(stockRepository.existsByProductId(1L)).thenReturn(false);
            when(productRepository.findById(1L)).thenReturn(Optional.of(product));

            var stock = new Stock();
            stock.setId(1L);
            stock.setQuantity(100);
            stock.setProduct(product);
            when(stockRepository.save(any(Stock.class))).thenReturn(stock);

            StockResponse result = stockService.createStock(request);

            assertNotNull(result);
            assertEquals(1L, result.id());
            assertEquals("Coca-cola", result.product().name());
            assertEquals(100, result.quantity());
            verify(stockRepository, times(1)).save(any(Stock.class));
        }

        @Test
        void shouldThrowConflictWhenStockAlreadyExists() {
            var request = new CreateStockDTO(1L, 50);

            when(stockRepository.existsByProductId(1L)).thenReturn(true);

            ConflictStockException exception = assertThrows(
                    ConflictStockException.class,
                    () -> stockService.createStock(request)
            );

            assertTrue(exception.getMessage().contains("O estoque deste produto já foi criado"));
            verify(stockRepository, never()).save(any());
        }

        @Test
        void shouldThrowNotFoundWhenProductDoesNotExist() {
            var request = new CreateStockDTO(99L, 50);

            when(stockRepository.existsByProductId(99L)).thenReturn(false);
            when(productRepository.findById(99L)).thenReturn(Optional.empty());

            NotFoundProductException exception = assertThrows(
                    NotFoundProductException.class,
                    () -> stockService.createStock(request)
            );

            assertTrue(exception.getMessage().contains("Produto com id 99 não foi encontrado"));
            verify(stockRepository, never()).save(any());
        }
    }

    @Nested
    class FindStockByIdTests {

        @Test
        void shouldReturnStockWhenExists() {
            var category = new Category(1L, "Bebidas");
            var product = new Product();
            product.setId(1L);
            product.setName("Coca-cola");
            product.setSalePrice(BigDecimal.valueOf(8.50));
            product.setCategory(category);

            var stock = new Stock();
            stock.setId(1L);
            stock.setQuantity(100);
            stock.setProduct(product);

            when(stockRepository.findById(1L)).thenReturn(Optional.of(stock));

            StockResponse result = stockService.findStockById(1L);

            assertNotNull(result);
            assertEquals(1L, result.id());
            assertEquals(100, result.quantity());
        }

        @Test
        void shouldThrowNotFoundWhenStockDoesNotExist() {
            when(stockRepository.findById(99L)).thenReturn(Optional.empty());

            NotFoundStockException exception = assertThrows(
                    NotFoundStockException.class,
                    () -> stockService.findStockById(99L)
            );

            assertTrue(exception.getMessage().contains("Estoque do id: 99 não encontrado"));
        }
    }

    @Nested
    class FindAllStocksTests {

        @Test
        void shouldReturnPaginatedStocks() {
            var category = new Category(1L, "Bebidas");
            var product = new Product();
            product.setId(1L);
            product.setName("Coca-cola");
            product.setSalePrice(BigDecimal.valueOf(8.50));
            product.setCategory(category);

            var stock = new Stock();
            stock.setId(1L);
            stock.setQuantity(100);
            stock.setProduct(product);

            var pageable = PageRequest.of(0, 5);
            when(stockRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(stock)));

            Page<StockResponse> result = stockService.findAllStocks(pageable);

            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            assertEquals(100, result.getContent().get(0).quantity());
        }

        @Test
        void shouldReturnEmptyPageWhenNoStocks() {
            var pageable = PageRequest.of(0, 5);
            when(stockRepository.findAll(pageable)).thenReturn(Page.empty());

            Page<StockResponse> result = stockService.findAllStocks(pageable);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class FindStockByProductIdTests {

        @Test
        void shouldReturnStockByProductId() {
            var category = new Category(1L, "Bebidas");
            var product = new Product();
            product.setId(1L);
            product.setName("Coca-cola");
            product.setSalePrice(BigDecimal.valueOf(8.50));
            product.setCategory(category);

            var stock = new Stock();
            stock.setId(1L);
            stock.setQuantity(50);
            stock.setProduct(product);

            when(stockRepository.findByProductId(1L)).thenReturn(Optional.of(stock));

            StockResponse result = stockService.findStockByProductId(1L);

            assertNotNull(result);
            assertEquals(50, result.quantity());
            assertEquals("Coca-cola", result.product().name());
        }

        @Test
        void shouldThrowNotFoundWhenStockByProductIdDoesNotExist() {
            when(stockRepository.findByProductId(99L)).thenReturn(Optional.empty());

            NotFoundStockException exception = assertThrows(
                    NotFoundStockException.class,
                    () -> stockService.findStockByProductId(99L)
            );

            assertTrue(exception.getMessage().contains("Estoque do produto id: 99 não encontrado"));
        }
    }
}
