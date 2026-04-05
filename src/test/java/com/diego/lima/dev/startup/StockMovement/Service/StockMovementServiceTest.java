package com.diego.lima.dev.startup.StockMovement.Service;

import com.diego.lima.dev.startup.Exceptions.Product.NotFoundProductException;
import com.diego.lima.dev.startup.Exceptions.Stock.ConflictStockException;
import com.diego.lima.dev.startup.Exceptions.Stock.NotFoundStockException;
import com.diego.lima.dev.startup.Product.Model.Product;
import com.diego.lima.dev.startup.Product.Repository.ProductRepository;
import com.diego.lima.dev.startup.Stock.Model.Stock;
import com.diego.lima.dev.startup.Stock.Repository.StockRepository;
import com.diego.lima.dev.startup.StockMovement.Dto.Request.StockMovementRequestDTO;
import com.diego.lima.dev.startup.StockMovement.Dto.Response.StockMovementResponseDTO;
import com.diego.lima.dev.startup.StockMovement.Model.MovementType;
import com.diego.lima.dev.startup.StockMovement.Model.StockMovement;
import com.diego.lima.dev.startup.StockMovement.Repository.StockMovementRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StockMovementServiceTest {

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private StockMovementService stockMovementService;

    public StockMovementServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    class CreateMovementTests {

        @Test
        void shouldCreateEntryMovementAndIncrementStock() {
            var product = new Product();
            product.setId(1L);
            product.setName("Coca-cola");

            var stock = new Stock();
            stock.setId(1L);
            stock.setQuantity(50);
            stock.setProduct(product);

            var request = new StockMovementRequestDTO(1L, 20, MovementType.ENTRY, "Compra fornecedor");

            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(stockRepository.findByProduct(product)).thenReturn(Optional.of(stock));
            when(stockMovementRepository.save(any(StockMovement.class))).thenAnswer(invocation -> {
                StockMovement movement = invocation.getArgument(0);
                movement.setId(1L);
                return movement;
            });

            StockMovementResponseDTO result = stockMovementService.createMovement(request);

            assertEquals(70, stock.getQuantity());
            assertEquals(20, result.quantity());
            assertEquals(MovementType.ENTRY, result.type());
            assertEquals("Compra fornecedor", result.reason());

            ArgumentCaptor<StockMovement> captor = ArgumentCaptor.forClass(StockMovement.class);
            verify(stockMovementRepository, times(1)).save(captor.capture());
            assertEquals(MovementType.ENTRY, captor.getValue().getType());
        }

        @Test
        void shouldCreateExitMovementAndDecrementStock() {
            var product = new Product();
            product.setId(1L);
            product.setName("Coca-cola");

            var stock = new Stock();
            stock.setId(1L);
            stock.setQuantity(50);
            stock.setProduct(product);

            var request = new StockMovementRequestDTO(1L, 10, MovementType.EXIT, "Venda");

            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(stockRepository.findByProduct(product)).thenReturn(Optional.of(stock));
            when(stockMovementRepository.save(any(StockMovement.class))).thenAnswer(invocation -> {
                StockMovement m = invocation.getArgument(0);
                m.setId(1L);
                return m;
            });

            StockMovementResponseDTO result = stockMovementService.createMovement(request);

            assertEquals(40, stock.getQuantity());
            assertEquals(10, result.quantity());
            assertEquals(MovementType.EXIT, result.type());
        }

        @Test
        void shouldThrowConflictWhenInsufficientStockForExit() {
            var product = new Product();
            product.setId(1L);
            product.setName("Coca-cola");

            var stock = new Stock();
            stock.setId(1L);
            stock.setQuantity(5);
            stock.setProduct(product);

            var request = new StockMovementRequestDTO(1L, 10, MovementType.EXIT, null);

            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(stockRepository.findByProduct(product)).thenReturn(Optional.of(stock));

            ConflictStockException exception = assertThrows(
                    ConflictStockException.class,
                    () -> stockMovementService.createMovement(request)
            );

            assertTrue(exception.getMessage().contains("Estoque insuficiente"));
        }

        @Test
        void shouldCreateLossMovementAndDecrementStock() {
            var product = new Product();
            product.setId(1L);
            product.setName("Coca-cola");

            var stock = new Stock();
            stock.setId(1L);
            stock.setQuantity(50);
            stock.setProduct(product);

            var request = new StockMovementRequestDTO(1L, 5, MovementType.LOSS, "Produto vencido");

            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(stockRepository.findByProduct(product)).thenReturn(Optional.of(stock));
            when(stockMovementRepository.save(any(StockMovement.class))).thenAnswer(invocation -> {
                StockMovement m = invocation.getArgument(0);
                m.setId(1L);
                return m;
            });

            StockMovementResponseDTO result = stockMovementService.createMovement(request);

            assertEquals(45, stock.getQuantity());
            assertEquals(MovementType.LOSS, result.type());
            assertEquals("Produto vencido", result.reason());
        }

        @Test
        void shouldThrowConflictWhenInsufficientStockForLoss() {
            var product = new Product();
            product.setId(1L);

            var stock = new Stock();
            stock.setId(1L);
            stock.setQuantity(3);
            stock.setProduct(product);

            var request = new StockMovementRequestDTO(1L, 10, MovementType.LOSS, null);

            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(stockRepository.findByProduct(product)).thenReturn(Optional.of(stock));

            assertThrows(ConflictStockException.class,
                    () -> stockMovementService.createMovement(request));
        }

        @Test
        void shouldCreateAdjustmentMovementAndSetStock() {
            var product = new Product();
            product.setId(1L);

            var stock = new Stock();
            stock.setId(1L);
            stock.setQuantity(50);
            stock.setProduct(product);

            var request = new StockMovementRequestDTO(1L, 100, MovementType.ADJUSTMENT, "Inventário");

            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(stockRepository.findByProduct(product)).thenReturn(Optional.of(stock));
            when(stockMovementRepository.save(any(StockMovement.class))).thenAnswer(invocation -> {
                StockMovement m = invocation.getArgument(0);
                m.setId(1L);
                return m;
            });

            StockMovementResponseDTO result = stockMovementService.createMovement(request);

            assertEquals(100, stock.getQuantity());  // adjustment sets exact value
            assertEquals(100, result.quantity());
            assertEquals(MovementType.ADJUSTMENT, result.type());
        }

        @Test
        void shouldThrowNotFoundWhenProductDoesNotExist() {
            var request = new StockMovementRequestDTO(99L, 10, MovementType.ENTRY, null);
            when(productRepository.findById(99L)).thenReturn(Optional.empty());

            NotFoundProductException exception = assertThrows(
                    NotFoundProductException.class,
                    () -> stockMovementService.createMovement(request)
            );

            assertTrue(exception.getMessage().contains("Produto com id 99 não encontrado"));
        }

        @Test
        void shouldThrowNotFoundWhenStockDoesNotExist() {
            var product = new Product();
            product.setId(1L);

            var request = new StockMovementRequestDTO(1L, 10, MovementType.ENTRY, null);
            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(stockRepository.findByProduct(product)).thenReturn(Optional.empty());

            NotFoundStockException exception = assertThrows(
                    NotFoundStockException.class,
                    () -> stockMovementService.createMovement(request)
            );

            assertTrue(exception.getMessage().contains("Estoque do produto 1 não encontrado"));
        }
    }

    @Nested
    class FindAllStockMovementTests {

        @Test
        void shouldReturnPaginatedMovements() {
            var m1 = new StockMovement();
            m1.setId(1L);
            m1.setQuantity(20);
            m1.setType(MovementType.ENTRY);
            m1.setReason("Compra");

            var m2 = new StockMovement();
            m2.setId(2L);
            m2.setQuantity(5);
            m2.setType(MovementType.EXIT);

            var pageable = PageRequest.of(0, 5);
            when(stockMovementRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(m1, m2)));

            Page<StockMovementResponseDTO> result = stockMovementService.findAllStockMovement(pageable);

            assertNotNull(result);
            assertEquals(2, result.getContent().size());
            assertEquals(20, result.getContent().get(0).quantity());
            assertEquals(MovementType.ENTRY, result.getContent().get(0).type());
        }

        @Test
        void shouldReturnEmptyPageWhenNoMovements() {
            var pageable = PageRequest.of(0, 5);
            when(stockMovementRepository.findAll(pageable)).thenReturn(Page.empty());

            Page<StockMovementResponseDTO> result = stockMovementService.findAllStockMovement(pageable);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }
}
