package com.diego.lima.dev.startup.Product.Controller;

import com.diego.lima.dev.startup.Category.Dtos.Request.CreateCategoryDTO;
import com.diego.lima.dev.startup.Product.Dtos.Request.CreateProductDTO;
import com.diego.lima.dev.startup.Product.Dtos.Request.UpdateProductDTO;
import com.diego.lima.dev.startup.Product.Dtos.Response.ProductResponse;
import com.diego.lima.dev.startup.Product.Service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.diego.lima.dev.startup.Exceptions.EntityNotFoundException;


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
            var response = new ProductResponse(1L, "Coca-cola", BigDecimal.valueOf(8.50), "Bebidas", 1L);

            when(productService.createProduct(any(CreateProductDTO.class))).thenReturn(response);

            mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.name").value("Coca-cola"))
                    .andExpect(jsonPath("$.salePrice").value(8.50))
                    .andExpect(jsonPath("$.category").value("Bebidas"))
                    .andExpect(jsonPath("$.categoryId").value(1L));

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
                    .andExpect(jsonPath("$.fields.name").exists())
                    .andExpect(jsonPath("$.fields.salePrice").exists())
                    .andExpect(jsonPath("$.fields.categoryId").exists());
        }

    }

    @Nested
    class FindAllProductsTests {

        @Test
        @DisplayName("GET /products - deve retornar 200 com produtos paginados")
        void shouldReturn200WithPaginatedProducts() throws Exception {
            var p1 = new ProductResponse(1L, "Coca-cola", BigDecimal.valueOf(8.50), "Bebidas", 1L);
            var p2 = new ProductResponse(2L, "Guarana", BigDecimal.valueOf(4.00), "Bebidas", 1L);
            when(productService.findAllProducts(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(p1, p2)));

            mockMvc.perform(get("/products")
                            .param("page", "0")
                            .param("size", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.content[0].name").value("Coca-cola"));
        }

        @Test
        @DisplayName("GET /products - deve retornar 200 com página vazia")
        void shouldReturn200WithEmptyPage() throws Exception {
            when(productService.findAllProducts(any(Pageable.class))).thenReturn(Page.empty());

            mockMvc.perform(get("/products")
                            .param("page", "0")
                            .param("size", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(0));
        }
    }

    @Nested
    class FindProductByIdTests {

        @Test
        @DisplayName("GET /products/{id} - deve retornar 200 com produto")
        void shouldReturn200WhenProductExists() throws Exception {
            var response = new ProductResponse(1L, "Coca-cola", BigDecimal.valueOf(8.50), "Bebidas", 1L);
            when(productService.findProductById(1L)).thenReturn(response);

            mockMvc.perform(get("/products/{id}", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.name").value("Coca-cola"))
                    .andExpect(jsonPath("$.salePrice").value(8.50));
        }

        @Test
        @DisplayName("GET /products/{id} - deve retornar 404 quando produto não encontrado")
        void shouldReturn404WhenProductNotFound() throws Exception {
            when(productService.findProductById(99L))
                    .thenThrow(new com.diego.lima.dev.startup.Exceptions.EntityNotFoundException("Produto com id 99 não encontrado"));

            mockMvc.perform(get("/products/{id}", 99L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").exists())
                    .andExpect(jsonPath("$.path").value("/products/99"));
        }
    }

    @Nested
    class UpdateProductTests {

        @Test
        @DisplayName("PATCH /products/{id} - deve retornar 204 ao atualizar")
        void shouldReturn204WhenUpdatingProduct() throws Exception {
            String json = """
                    {"name": "Coca-Cola 2L"}
                    """;

            mockMvc.perform(patch("/products/{id}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isNoContent());

            verify(productService).updateProduct(eq(1L), any(UpdateProductDTO.class));
        }

        @Test
        @DisplayName("PATCH /products/{id} - deve retornar 404 quando não encontrado")
        void shouldReturn404WhenProductNotFound() throws Exception {
            String json = """
                    {"name": "Test"}
                    """;

            doThrow(new com.diego.lima.dev.startup.Exceptions.EntityNotFoundException("Produto com id 1 não encontrado"))
                    .when(productService).updateProduct(eq(1L), any(UpdateProductDTO.class));

            mockMvc.perform(patch("/products/{id}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }
    }

    @Nested
    class DeleteProductTests {

        @Test
        @DisplayName("DELETE /products/{id} - deve retornar 204 ao deletar")
        void shouldReturn204WhenDeletingProduct() throws Exception {
            mockMvc.perform(delete("/products/{id}", 1L))
                    .andExpect(status().isNoContent());

            verify(productService).deleteProduct(1L);
        }

        @Test
        @DisplayName("DELETE /products/{id} - deve retornar 404 quando não encontrado")
        void shouldReturn404WhenDeletingNonExistentProduct() throws Exception {
            doThrow(new com.diego.lima.dev.startup.Exceptions.EntityNotFoundException("Produto com id 1 não encontrado"))
                    .when(productService).deleteProduct(1L);

            mockMvc.perform(delete("/products/{id}", 1L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }
    }
}
