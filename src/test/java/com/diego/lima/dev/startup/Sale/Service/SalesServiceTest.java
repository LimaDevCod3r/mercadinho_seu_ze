package com.diego.lima.dev.startup.Sale.Service;

import com.diego.lima.dev.startup.Product.Model.Product;
import com.diego.lima.dev.startup.Product.Repository.ProductRepository;
import com.diego.lima.dev.startup.Sale.Dto.Request.SaleRequestDTO;
import com.diego.lima.dev.startup.Sale.Dto.Response.SaleResponseDTO;
import com.diego.lima.dev.startup.Sale.Model.Sale;
import com.diego.lima.dev.startup.Sale.Repository.SalesRepository;
import com.diego.lima.dev.startup.SalesItems.Dto.Request.SaleItemRequestDTO;
import com.diego.lima.dev.startup.SalesItems.Model.SaleItem;
import com.diego.lima.dev.startup.SalesItems.Repository.SaleItemRepository;
import com.diego.lima.dev.startup.Stock.Model.Stock;
import com.diego.lima.dev.startup.Stock.Repository.StockRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SalesServiceTest {

    @Mock
    private SalesRepository salesRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SaleItemRepository saleItemRepository;

    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private SalesService salesService;

    public SalesServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    class CreateSaleTests {

        @Test
        void shouldCreateSaleSuccessfully() {
            var product = new Product();
            product.setId(1L);
            product.setName("Coca-cola");
            product.setSalePrice(BigDecimal.valueOf(10.00));

            var stock = new Stock();
            stock.setId(1L);
            stock.setProduct(product);
            stock.setQuantity(50);

            var savedSale = new Sale();
            savedSale.setId(1L);
            savedSale.setSaleDate(LocalDateTime.now());
            savedSale.setTotal(BigDecimal.ZERO);

            var savedItem = new SaleItem();
            savedItem.setId(1L);
            savedItem.setSale(savedSale);
            savedItem.setProduct(product);
            savedItem.setQuantity(2);
            savedItem.setUnitPrice(BigDecimal.valueOf(10.00));
            savedItem.setTotalPrice(BigDecimal.valueOf(20.00));

            var items = List.of(new SaleItemRequestDTO(1L, 2));
            var request = new SaleRequestDTO(BigDecimal.valueOf(50.00), items);

            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(stockRepository.findByProduct(product)).thenReturn(Optional.of(stock));
            when(salesRepository.save(any(Sale.class))).thenReturn(savedSale);
            when(saleItemRepository.save(any(SaleItem.class))).thenReturn(savedItem);

            SaleResponseDTO result = salesService.createSale(request);

            assertNotNull(result);
            assertEquals(1L, result.saleId());
            assertEquals(BigDecimal.valueOf(20.00), result.total());
            assertEquals(BigDecimal.valueOf(50.00), result.amountPaid());
            assertEquals(BigDecimal.valueOf(30.00), result.change());
            assertEquals(1, result.items().size());


            assertEquals(48, stock.getQuantity());
        }

        @Test
        void shouldCreateSaleWithMultipleItems() {
            var p1 = new Product();
            p1.setId(1L);
            p1.setName("Coca-cola");
            p1.setSalePrice(BigDecimal.valueOf(10.00));

            var p2 = new Product();
            p2.setId(2L);
            p2.setName("Guarana");
            p2.setSalePrice(BigDecimal.valueOf(8.00));

            var s1 = new Stock();
            s1.setId(1L);
            s1.setProduct(p1);
            s1.setQuantity(100);

            var s2 = new Stock();
            s2.setId(2L);
            s2.setProduct(p2);
            s2.setQuantity(100);

            var savedSale = new Sale();
            savedSale.setId(1L);
            savedSale.setSaleDate(LocalDateTime.now());
            savedSale.setTotal(BigDecimal.ZERO);

            var items = List.of(
                    new SaleItemRequestDTO(1L, 3),
                    new SaleItemRequestDTO(2L, 2)
            );

            // total = 3*10 + 2*8 = 46
            var request = new SaleRequestDTO(BigDecimal.valueOf(50.00), items);

            when(productRepository.findById(1L)).thenReturn(Optional.of(p1));
            when(productRepository.findById(2L)).thenReturn(Optional.of(p2));
            when(stockRepository.findByProduct(p1)).thenReturn(Optional.of(s1));
            when(stockRepository.findByProduct(p2)).thenReturn(Optional.of(s2));
            when(salesRepository.save(any(Sale.class))).thenReturn(savedSale);
            when(saleItemRepository.save(any(SaleItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

            SaleResponseDTO result = salesService.createSale(request);

            assertEquals(BigDecimal.valueOf(46.00), result.total());
            assertEquals(BigDecimal.valueOf(4.00), result.change());
            assertEquals(2, result.items().size());
            assertEquals(97, s1.getQuantity());
            assertEquals(98, s2.getQuantity());
        }

        @Test
        void shouldThrowWhenProductNotFound() {
            var items = List.of(new SaleItemRequestDTO(99L, 1));
            var request = new SaleRequestDTO(BigDecimal.valueOf(10.00), items);

            when(productRepository.findById(99L)).thenReturn(Optional.empty());
            when(salesRepository.save(any(Sale.class))).thenReturn(new Sale());

            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> salesService.createSale(request)
            );

            assertTrue(exception.getMessage().contains("Produto não encontrado"));
        }

        @Test
        void shouldThrowWhenStockNotEnough() {
            var product = new Product();
            product.setId(1L);
            product.setName("Coca-cola");
            product.setSalePrice(BigDecimal.valueOf(10.00));

            var stock = new Stock();
            stock.setId(1L);
            stock.setProduct(product);
            stock.setQuantity(2);  // only 2 in stock

            var items = List.of(new SaleItemRequestDTO(1L, 5));  // trying to buy 5
            var request = new SaleRequestDTO(BigDecimal.valueOf(50.00), items);

            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(stockRepository.findByProduct(product)).thenReturn(Optional.of(stock));
            when(salesRepository.save(any(Sale.class))).thenReturn(new Sale());

            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> salesService.createSale(request)
            );

            assertTrue(exception.getMessage().contains("Estoque insuficiente"));
        }

        @Test
        void shouldThrowWhenAmountPaidIsInsufficient() {
            var product = new Product();
            product.setId(1L);
            product.setName("Coca-cola");
            product.setSalePrice(BigDecimal.valueOf(10.00));

            var stock = new Stock();
            stock.setId(1L);
            stock.setProduct(product);
            stock.setQuantity(50);

            var savedSale = new Sale();
            savedSale.setId(1L);
            savedSale.setSaleDate(LocalDateTime.now());
            savedSale.setTotal(BigDecimal.ZERO);

            // 2 * 10 = 20 total, but only paying 15
            var items = List.of(new SaleItemRequestDTO(1L, 2));
            var request = new SaleRequestDTO(BigDecimal.valueOf(15.00), items);

            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(stockRepository.findByProduct(product)).thenReturn(Optional.of(stock));
            when(salesRepository.save(any(Sale.class))).thenReturn(savedSale);
            when(saleItemRepository.save(any(SaleItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> salesService.createSale(request)
            );

            assertTrue(exception.getMessage().contains("Valor pago insuficiente"));
        }
    }
}
