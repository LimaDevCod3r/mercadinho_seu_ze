package com.diego.lima.dev.startup.Product.Controller;

import com.diego.lima.dev.startup.Category.Dtos.Request.CreateCategoryDTO;
import com.diego.lima.dev.startup.Product.Dtos.Request.CreateProductDTO;
import com.diego.lima.dev.startup.Product.Dtos.Response.ProductResponse;
import com.diego.lima.dev.startup.Product.Service.ProductService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @Nested
    class CreateProductTests {

        @Test
        @DisplayName("POST /produtos - deve retornar 201 com produto criado")
        void shouldReturn201WhenProductIsCreated() throws Exception {
            var request = new CreateProductDTO("Coca-cola", BigDecimal.valueOf(8.50), 1L);
            var response = new ProductResponse(1L, "Coca-cola", BigDecimal.valueOf(8.50), "Bebidas");

            when(productService.create(any(CreateProductDTO.class))).thenReturn(response);

            mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.name").value("Coca-cola"))
                    .andExpect(jsonPath("$.salePrice").value(8.50))
                    .andExpect(jsonPath("$.category").value("Bebidas"));

        }

        @Test
        @DisplayName("Deve retorna 400 quando campo corpo da requsição for inválido")
        void shouldReturn400WhenFieldToBodyIsInvalid() throws Exception {

            String invalidJson = """
                        {
                            "name": "",
                            "salePrice": -5,
                            "categoryId": 0
                        }
                    """;

            mockMvc.perform(post("/products")  // endpoint de produto
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.fields.name").value("Produto precisa de um nome válido"))
                    .andExpect(jsonPath("$.fields.salePrice").value("O preço deve ser maior que zero"))
                    .andExpect(jsonPath("$.fields.categoryId").value("O ID da categoria deve ser maior que zero"));
        }

    }
}
