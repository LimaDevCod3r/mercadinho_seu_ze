package com.diego.lima.dev.startup.Category.Controller;


import com.diego.lima.dev.startup.Category.Dtos.Request.CreateCategoryDTO;
import com.diego.lima.dev.startup.Category.Dtos.Request.UpdateCategoryDTO;
import com.diego.lima.dev.startup.Category.Dtos.Response.CategoryResponse;
import com.diego.lima.dev.startup.Category.Service.CategoryService;
import com.diego.lima.dev.startup.Exceptions.Category.NotFoundCategoryException;
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

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


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
        @DisplayName("POST /categorias - deve retornar 201 com a categoria criada")
        void shouldReturn201WhenCategoryIsCreated() throws Exception {
            var request = new CreateCategoryDTO("Bebidas");
            var response = new CategoryResponse(1L, "Bebidas");

            when(categoryService.create(any(CreateCategoryDTO.class))).thenReturn(response);

            mockMvc.perform(post("/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.name").value("Bebidas"));
        }

        @Test
        @DisplayName("POST /categorias - deve retornar 400 quando o campo da requisição for inválido")
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
                    .andExpect(jsonPath("$.fields.name").exists())
                    .andExpect(jsonPath("$.fields.name")
                            .value("Produto precisa de um nome válido"));
        }
    }

    @Nested
    class FindAllCategoryTest {

        @Test
        @DisplayName("GET /categorias - deve retornar 200 com uma lista de categorias")
        void shouldReturn200WhenListingAllCategories() throws Exception {
            List<CategoryResponse> categories = List.of(
                    new CategoryResponse(1L, "Bebidas"),
                    new CategoryResponse(2L, "Comidas")
            );

            Page<CategoryResponse> page = new PageImpl<>(categories);

            when(categoryService.findAll(any(Pageable.class))).thenReturn(page);

            // 4. Executando requisição
            mockMvc.perform(get("/categories")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())

                    // 5. Validando JSON
                    .andExpect(jsonPath("$.content[0].name").value("Bebidas"))
                    .andExpect(jsonPath("$.content[1].name").value("Comidas"))
                    .andExpect(jsonPath("$.content.length()").value(2));
        }

        @Test
        @DisplayName("GET /categorias - deve retornar 200 com uma lista de categorias vazias")
        void shouldReturn200WhenListingAllCategoriesIsEmpty() throws Exception {

            List<CategoryResponse> categories = List.of();

            Page<CategoryResponse> page = new PageImpl<>(categories);

            when(categoryService.findAll(any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get("/categories")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(0));

        }
    }

    @Nested
    class UpdateCategoryById {

        @Test
        @DisplayName("PATCH /categorias - Deve retorna 204 quando atualizar uma categoria")
        void shouldRetorn204WhenUpdatingCategory() throws Exception {
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
            verify(categoryService).updateById(eq(id), any(UpdateCategoryDTO.class));
        }

        @Test
        @DisplayName("PATCH /categories - Deve retornar 404 quando não encontrar categoria")
        void shouldReturn404WhenNotFoundIdCategory() throws Exception {

            Long id = 1L;

            String json = """
                        {
                            "name": "Comidas"
                        }
                    """;

            doThrow(new NotFoundCategoryException(
                    String.format("ID: %s não foi encontrado.", id)
            )).when(categoryService).updateById(eq(id), any(UpdateCategoryDTO.class));

            mockMvc.perform(patch("/categories/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").exists())
                    .andExpect(jsonPath("$.path").value("/categories/" + id));
        }

        @Test
        @DisplayName("Deve retorna 400 quando id é inválido")
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
        @DisplayName("Deve retorna 202 quando deleta uma categoria por Id for um sucesso")
        void shouldReturn204WhenDeleteCategoryWasSuccessful() throws Exception {

            Long id = 1L;
            mockMvc.perform(delete("/categories/{id}", id))
                    .andExpect(status().isNoContent());

            verify(categoryService).deleteById(id);
        }

        @Test
        @DisplayName("Deve retorna 404 quando categoria não encontrada.")
        void shouldReturn404WhenCategoryNotFound() throws Exception {

            Long id = 1L;

            doThrow(new NotFoundCategoryException(
                    String.format("ID: %s não foi encontrado.", id)
            )).when(categoryService).deleteById(id);

            mockMvc.perform(delete("/categories/{id}", id))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").exists())
                    .andExpect(jsonPath("$.path").value("/categories/" + id));
        }
    }
}
