package com.diego.lima.dev.startup.Category.Controller;

import com.diego.lima.dev.startup.Category.Dtos.Request.CreateCategoryDTO;
import com.diego.lima.dev.startup.Category.Dtos.Request.UpdateCategoryDTO;
import com.diego.lima.dev.startup.Category.Dtos.Response.CategoryResponse;
import com.diego.lima.dev.startup.Category.Service.CategoryService;
import com.diego.lima.dev.startup.Exceptions.EntityNotFoundException;
import com.diego.lima.dev.startup.Product.Dtos.Response.ProductResponse;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(CategoryController.class)
public class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;


    @Nested
    class CreateCategoryTests {
        @Test
        @DisplayName("POST /categories - deve retornar 201 com a categoria criada")
        void shouldReturn201WhenCategoryIsCreated() throws Exception {
            var request = new CreateCategoryDTO("Bebidas");
            var response = new CategoryResponse(1L, "Bebidas");

            when(categoryService.createCategory(any(CreateCategoryDTO.class))).thenReturn(response);

            mockMvc.perform(post("/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.name").value("Bebidas"));
        }

        @Test
        @DisplayName("POST /categories - deve retornar 400 quando o campo da requisição for inválido")
        void shouldReturn400WhenFieldToBodyIsInvalid() throws Exception {

            String invalidJson = """
                        {
                            "name": ""
                        }
                    """;
            mockMvc.perform(post("/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.fields.name").exists());
        }
    }

    @Nested
    class FindAllCategoryTest {

        @Test
        @DisplayName("GET /categories - deve retornar 200 com array de categorias")
        void shouldReturn200WhenListingAllCategories() throws Exception {
            List<CategoryResponse> categories = List.of(
                    new CategoryResponse(1L, "Bebidas"),
                    new CategoryResponse(2L, "Comidas")
            );

            when(categoryService.findAllCategories()).thenReturn(categories);

            mockMvc.perform(get("/categories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].name").value("Bebidas"))
                    .andExpect(jsonPath("$[1].name").value("Comidas"))
                    .andExpect(jsonPath("$.length()").value(2));
        }

        @Test
        @DisplayName("GET /categories - deve retornar 200 com lista vazia")
        void shouldReturn200WhenListingAllCategoriesIsEmpty() throws Exception {
            when(categoryService.findAllCategories()).thenReturn(List.of());

            mockMvc.perform(get("/categories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    @Nested
    class FindProductsByCategoryIdTest {

        @Test
        @DisplayName("GET /categories/{id}/products - deve retornar 200 com produtos paginados")
        void shouldReturn200WithPaginatedProducts() throws Exception {
            Long categoryId = 1L;
            List<ProductResponse> products = List.of(
                    new ProductResponse(10L, "Coca-Cola", new BigDecimal("5.50"), "Bebidas", 1L)
            );
            Page<ProductResponse> page = new PageImpl<>(products);

            when(categoryService.findProductsByCategory(eq(categoryId), any(Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get("/categories/{id}/products", categoryId)
                            .param("page", "0")
                            .param("size", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].name").value("Coca-Cola"))
                    .andExpect(jsonPath("$.content[0].salePrice").value(5.50))
                    .andExpect(jsonPath("$.content[0].category").value("Bebidas"))
                    .andExpect(jsonPath("$.content.length()").value(1));
        }

        @Test
        @DisplayName("GET /categories/{id}/products - deve retornar 404 quando categoria não existir")
        void shouldReturn404WhenCategoryNotFound() throws Exception {
            Long categoryId = 99L;

            when(categoryService.findProductsByCategory(eq(categoryId), any(Pageable.class)))
                    .thenThrow(new EntityNotFoundException(
                            String.format("A categoria do ID: %s não foi encontrada.", categoryId)
                    ));

            mockMvc.perform(get("/categories/{id}/products", categoryId)
                            .param("page", "0")
                            .param("size", "5"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").exists())
                    .andExpect(jsonPath("$.path").value("/categories/" + categoryId + "/products"));
        }
    }

    @Nested
    class UpdateCategoryById {

        @Test
        @DisplayName("PATCH /categories/{id} - deve retornar 204 quando atualizar uma categoria")
        void shouldReturn204WhenUpdatingCategory() throws Exception {
            Long id = 1L;

            String json = """
                        {
                            "name": "Comidas"
                        }
                    """;

            mockMvc.perform(patch("/categories/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isNoContent());
            verify(categoryService).updateCategory(eq(id), any(UpdateCategoryDTO.class));
        }

        @Test
        @DisplayName("PATCH /categories/{id} - deve retornar 404 quando não encontrar categoria")
        void shouldReturn404WhenNotFoundIdCategory() throws Exception {

            Long id = 1L;

            String json = """
                        {
                            "name": "Comidas"
                        }
                    """;

            doThrow(new EntityNotFoundException(
                    String.format("ID: %s não foi encontrado.", id)
            )).when(categoryService).updateCategory(eq(id), any(UpdateCategoryDTO.class));

            mockMvc.perform(patch("/categories/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").exists())
                    .andExpect(jsonPath("$.path").value("/categories/" + id));
        }

        @Test
        @DisplayName("PATCH /categories/abc - deve retornar 400 quando id for inválido")
        void shouldReturn400WhenIdIsInvalid() throws Exception {

            mockMvc.perform(patch("/categories/abc")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                        {
                                            "name": "Comidas"
                                        }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").exists());
        }
    }

    @Nested
    class DeleteCategoryById {

        @Test
        @DisplayName("DELETE /categories/{id} - deve retornar 204 quando deletar categoria com sucesso")
        void shouldReturn204WhenDeleteCategoryWasSuccessful() throws Exception {

            Long id = 1L;
            mockMvc.perform(delete("/categories/{id}", id))
                    .andExpect(status().isNoContent());

            verify(categoryService).deleteCategory(id);
        }

        @Test
        @DisplayName("DELETE /categories/{id} - deve retornar 404 quando categoria não encontrada")
        void shouldReturn404WhenCategoryNotFound() throws Exception {

            Long id = 1L;

            doThrow(new EntityNotFoundException(
                    String.format("ID: %s não foi encontrado.", id)
            )).when(categoryService).deleteCategory(id);

            mockMvc.perform(delete("/categories/{id}", id))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").exists())
                    .andExpect(jsonPath("$.path").value("/categories/" + id));
        }
    }
}
