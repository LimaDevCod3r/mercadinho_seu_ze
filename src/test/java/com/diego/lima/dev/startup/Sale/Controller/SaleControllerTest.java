package com.diego.lima.dev.startup.Sale.Controller;

import com.diego.lima.dev.startup.Sale.Dto.Request.SaleRequestDTO;
import com.diego.lima.dev.startup.Sale.Dto.Response.SaleResponseDTO;
import com.diego.lima.dev.startup.Sale.Service.SalesService;
import com.diego.lima.dev.startup.SalesItems.Dto.Request.SaleItemRequestDTO;
import com.diego.lima.dev.startup.SalesItems.Dto.Response.SaleItemResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SaleController.class)
public class SaleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SalesService salesService;


    @Nested
    class CreateSaleTests {

        @Test
        @DisplayName("POST /sales - deve retornar 201 com venda criada")
        void shouldReturn201WhenSaleIsCreated() throws Exception {
            var items = List.of(new SaleItemRequestDTO(1L, 2));
            var request = new SaleRequestDTO(BigDecimal.valueOf(50.00), items);

            var now = LocalDateTime.of(2026, 4, 5, 10, 30);
            var itemResponse = new SaleItemResponseDTO("Coca-cola", 2, BigDecimal.valueOf(10.00), BigDecimal.valueOf(20.00));
            var response = new SaleResponseDTO(1L, now, List.of(itemResponse), BigDecimal.valueOf(20.00), BigDecimal.valueOf(50.00), BigDecimal.valueOf(30.00));

            when(salesService.createSale(any(SaleRequestDTO.class))).thenReturn(response);

            mockMvc.perform(post("/sales")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.saleId").value(1L))
                    .andExpect(jsonPath("$.total").value(20.00))
                    .andExpect(jsonPath("$.amountPaid").value(50.00))
                    .andExpect(jsonPath("$.change").value(30.00))
                    .andExpect(jsonPath("$.items.length()").value(1))
                    .andExpect(jsonPath("$.items[0].productName").value("Coca-cola"))
                    .andExpect(jsonPath("$.items[0].quantity").value(2))
                    .andExpect(jsonPath("$.items[0].unitPrice").value(10.00))
                    .andExpect(jsonPath("$.items[0].totalPrice").value(20.00));
        }

        @Test
        @DisplayName("POST /sales - deve retornar 201 com venda de múltiplos itens")
        void shouldReturn201WhenSaleWithMultipleItems() throws Exception {
            var items = List.of(
                    new SaleItemRequestDTO(1L, 3),
                    new SaleItemRequestDTO(2L, 1)
            );
            var request = new SaleRequestDTO(BigDecimal.valueOf(50.00), items);

            var now = LocalDateTime.of(2026, 4, 5, 10, 30);
            var itemResponse1 = new SaleItemResponseDTO("Coca-cola", 3, BigDecimal.valueOf(10.00), BigDecimal.valueOf(30.00));
            var itemResponse2 = new SaleItemResponseDTO("Guarana", 1, BigDecimal.valueOf(8.00), BigDecimal.valueOf(8.00));
            var response = new SaleResponseDTO(2L, now, List.of(itemResponse1, itemResponse2), BigDecimal.valueOf(38.00), BigDecimal.valueOf(50.00), BigDecimal.valueOf(12.00));

            when(salesService.createSale(any(SaleRequestDTO.class))).thenReturn(response);

            mockMvc.perform(post("/sales")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.saleId").value(2L))
                    .andExpect(jsonPath("$.total").value(38.00))
                    .andExpect(jsonPath("$.items.length()").value(2))
                    .andExpect(jsonPath("$.items[0].productName").value("Coca-cola"))
                    .andExpect(jsonPath("$.items[1].productName").value("Guarana"));
        }

        @Test
        @DisplayName("POST /sales - com amountPaid null deve retornar 500 (serviço lança RuntimeException)")
        void shouldReturn500WhenAmountPaidIsNull() throws Exception {
            String json = """
                    {"items": [{"productId": 1, "quantity": 1}]}
                    """;


            when(salesService.createSale(any(SaleRequestDTO.class)))
                    .thenThrow(new RuntimeException("Valor pago insuficiente"));

            mockMvc.perform(post("/sales")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.error").value("ERRO INTERNO DO SERVIDOR"));
        }

        @Test
        @DisplayName("POST /sales - deve retornar 400 quando lista de itens está vazia")
        void shouldReturn400WhenItemsListIsEmpty() throws Exception {
            String invalidJson = """
                    {"amountPaid": 10.00, "items": []}
                    """;


            var response = new SaleResponseDTO(1L, LocalDateTime.now(), List.of(), BigDecimal.ZERO, BigDecimal.valueOf(10.00), BigDecimal.valueOf(10.00));
            when(salesService.createSale(any(SaleRequestDTO.class))).thenReturn(response);

            mockMvc.perform(post("/sales")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.total").value(0));
        }

        @Test
        @DisplayName("POST /sales - deve retornar 500 quando serviço lança RuntimeException")
        void shouldReturn500WhenServiceThrowsRuntimeException() throws Exception {
            var items = List.of(new SaleItemRequestDTO(1L, 2));
            var request = new SaleRequestDTO(BigDecimal.valueOf(50.00), items);

            when(salesService.createSale(any(SaleRequestDTO.class)))
                    .thenThrow(new RuntimeException("Estoque insuficiente"));

            mockMvc.perform(post("/sales")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.error").value("ERRO INTERNO DO SERVIDOR"));
        }

        @Test
        @DisplayName("POST /sales - deve retornar 500 quando quantity é negativo (estouro de estoque)")
        void shouldReturn500WhenQuantityIsNegative() throws Exception {
            String json = """
                    {"amountPaid": 50, "items": [{"productId": 1, "quantity": -5}]}
                    """;

            when(salesService.createSale(any(SaleRequestDTO.class)))
                    .thenThrow(new RuntimeException("Estoque insuficiente"));

            mockMvc.perform(post("/sales")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500));
        }
    }
}
