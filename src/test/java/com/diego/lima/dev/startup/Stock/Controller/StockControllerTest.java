package com.diego.lima.dev.startup.Stock.Controller;

import com.diego.lima.dev.startup.Stock.Dto.Request.CreateStockDTO;
import com.diego.lima.dev.startup.Stock.Dto.Response.StockResponse;
import com.diego.lima.dev.startup.Stock.Service.StockService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.diego.lima.dev.startup.Exceptions.EntityNotFoundException;

@WebMvcTest(StockController.class)
public class StockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StockService stockService;


    @Nested
    class CreateStockTests {

        @Test
        @DisplayName("POST /stocks - deve retornar 201 com estoque criado")
        void shouldReturn201WhenStockIsCreated() throws Exception {
            var request = new CreateStockDTO(1L, 100);
            var response = new StockResponse(1L,
                    new com.diego.lima.dev.startup.Product.Dtos.Response.ProductResponse(1L, "Coca-cola", BigDecimal.valueOf(8.50), "Bebidas", 1L),
                    100);

            when(stockService.createStock(any(CreateStockDTO.class))).thenReturn(response);

            mockMvc.perform(post("/stocks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.quantity").value(100))
                    .andExpect(jsonPath("$.product.name").value("Coca-cola"));
        }

        @Test
        @DisplayName("POST /stocks - deve retornar 400 quando corpo inválido")
        void shouldReturn400WhenBodyIsInvalid() throws Exception {
            String invalidJson = """
                    {"productId": 0, "quantity": -5}
                    """;

            mockMvc.perform(post("/stocks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));
        }
    }

    @Nested
    class FindStockByIdTests {

        @Test
        @DisplayName("GET /stocks/{id} - deve retornar 200 com estoque")
        void shouldReturn200WhenStockFound() throws Exception {
            var response = new StockResponse(1L,
                    new com.diego.lima.dev.startup.Product.Dtos.Response.ProductResponse(1L, "Coca-cola", BigDecimal.valueOf(8.50), "Bebidas", 1L),
                    100);
            when(stockService.findStockById(1L)).thenReturn(response);

            mockMvc.perform(get("/stocks/{id}", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.quantity").value(100));
        }

        @Test
        @DisplayName("GET /stocks/{id} - deve retornar 404 quando estoque não encontrado")
        void shouldReturn404WhenStockNotFound() throws Exception {
            when(stockService.findStockById(99L))
                    .thenThrow(new EntityNotFoundException("Estoque do id: 99 não encontrado"));

            mockMvc.perform(get("/stocks/{id}", 99L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").exists())
                    .andExpect(jsonPath("$.path").value("/stocks/99"));
        }
    }

    @Nested
    class FindAllStocksTests {

        @Test
        @DisplayName("GET /stocks - deve retornar 200 com estoques paginados")
        void shouldReturn200WithPaginatedStocks() throws Exception {
            var response = new StockResponse(1L,
                    new com.diego.lima.dev.startup.Product.Dtos.Response.ProductResponse(1L, "Coca-cola", BigDecimal.valueOf(8.50), "Bebidas", 1L),
                    100);
            when(stockService.findAllStocks(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(response)));

            mockMvc.perform(get("/stocks")
                            .param("page", "0")
                            .param("size", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.content[0].quantity").value(100));
        }
    }

    @Nested
    class FindStockByProductIdTests {

        @Test
        @DisplayName("GET /stocks/product/{id} - deve retornar 200 com estoque do produto")
        void shouldReturn200WhenStockByProductIdFound() throws Exception {
            var response = new StockResponse(1L,
                    new com.diego.lima.dev.startup.Product.Dtos.Response.ProductResponse(1L, "Coca-cola", BigDecimal.valueOf(8.50), "Bebidas", 1L),
                    100);
            when(stockService.findStockByProductId(1L)).thenReturn(response);

            mockMvc.perform(get("/stocks/product/{id}", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.quantity").value(100));
        }

        @Test
        @DisplayName("GET /stocks/product/{id} - deve retornar 404 quando estoque não encontrado")
        void shouldReturn404WhenStockByProductIdNotFound() throws Exception {
            when(stockService.findStockByProductId(99L))
                    .thenThrow(new EntityNotFoundException("Estoque do produto id: 99 não encontrado"));

            mockMvc.perform(get("/stocks/product/{id}", 99L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }
    }
}
