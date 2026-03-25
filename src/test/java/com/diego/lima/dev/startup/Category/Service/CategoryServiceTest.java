package com.diego.lima.dev.startup.Category.Service;

import com.diego.lima.dev.startup.Category.Dtos.Request.CreateCategoryDTO;
import com.diego.lima.dev.startup.Category.Dtos.Request.UpdateCategoryDTO;
import com.diego.lima.dev.startup.Category.Dtos.Response.CategoryResponse;
import com.diego.lima.dev.startup.Category.Model.Category;
import com.diego.lima.dev.startup.Category.Repository.CategoryRepository;

import com.diego.lima.dev.startup.Exceptions.Category.ConflictCategoryException;
import com.diego.lima.dev.startup.Exceptions.Category.NotFoundCategoryException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class CategoryServiceTest {

    @Mock
    private CategoryRepository repository;

    @InjectMocks
    private CategoryService service;

    public CategoryServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    class CreateCategoryTests {

        @Test
        void shouldCreateCategory() {
            CreateCategoryDTO dto = new CreateCategoryDTO("Bebidas");

            when(repository.existsByName("Bebidas")).thenReturn(false);

            Category savedCategory = new Category();
            savedCategory.setName("Bebidas");

            when(repository.save(any(Category.class))).thenReturn(savedCategory);

            CategoryResponse result = service.create(dto);

            assertNotNull(result);
            assertEquals("Bebidas", result.name());

            verify(repository, times(1)).save(any(Category.class));
        }

        @Test
        void shouldNotBePossibleCreateCategoryWithSameName() {

            CreateCategoryDTO dto = new CreateCategoryDTO("Bebidas");

            when(repository.existsByName("Bebidas")).thenReturn(true);

            ConflictCategoryException exception = assertThrows(
                    ConflictCategoryException.class,
                    () -> service.create(dto)
            );

            assertEquals(
                    "Categoria: Bebidas já existe",
                    exception.getMessage()
            );

            verify(repository, never()).save(any(Category.class));
        }
    }


    @Nested
    class FindAllCategoryTest {

        @Test
        void shouldReturnAllCategories() {

            // Arrange
            Category category1 = new Category(1L, "Alimentação");
            Category category2 = new Category(2L, "Bebidas");

            List<Category> categoryList = List.of(category1, category2);

            Page<Category> page = new PageImpl<>(categoryList);

            Pageable pageable = PageRequest.of(0, 10);


            when(repository.findAll(pageable)).thenReturn(page);


            Page<CategoryResponse> result = service.findAll(pageable);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.getContent().size());

            assertEquals("Alimentação", result.getContent().get(0).name());
            assertEquals("Bebidas", result.getContent().get(1).name());

            verify(repository, times(1)).findAll(pageable);
        }

        @Test
        void shouldReturnEmptyPageWhenNoCategories() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);

            Page<Category> emptyPage = new PageImpl<>(List.of());

            when(repository.findAll(pageable)).thenReturn(emptyPage);

            // Act
            Page<CategoryResponse> result = service.findAll(pageable);

            // Assert
            assertNotNull(result);
            assertTrue(result.getContent().isEmpty());
            assertEquals(0, result.getTotalElements());

            verify(repository, times(1)).findAll(pageable);
        }

    }

    @Nested
    class UpdateCategoryById {

        @Test
        void shouldPartialUpdateByIdCategory() {
            Long id = 1L;
            Category category = new Category(id, "Bebidas");

            UpdateCategoryDTO request = new UpdateCategoryDTO("Comidas");

            when(repository.findById(id)).thenReturn(Optional.of(category));

            service.updateById(id, request);

            assertEquals("Comidas", category.getName());

            verify(repository).save(category);
        }

        @Test
        void shouldThrowNotFoundCategoryExceptionWhenIdIsNotFound() {

            Long id = 1L;

            UpdateCategoryDTO request = new UpdateCategoryDTO("Comidas");

            when(repository.findById(id)).thenReturn(Optional.empty());

            NotFoundCategoryException exception = assertThrows(
                    NotFoundCategoryException.class,
                    () -> service.updateById(id, request)
            );

            assertEquals(
                    String.format("A categoria do ID: %s não foi encontrado.", id),
                    exception.getMessage()
            );

            verify(repository, never()).save(any());
        }
    }

    @Nested
    class DeleteCategoryById {

        @Test
        void shouldDeleteCategoryById() {
            Long id = 1L;
            Category category = new Category(id, "Bebidas");

            when(repository.findById(id)).thenReturn(Optional.of(category));

            service.deleteById(id);

            verify(repository).deleteById(id);
        }

        @Test
        void shouldThrowNotFoundExceptionWhenToDeleteCategoryByIdNotExist() {
            Long id = 1L;
            when(repository.findById(id)).thenReturn(Optional.empty());

            NotFoundCategoryException exception = assertThrows(
                    NotFoundCategoryException.class,
                    () -> service.deleteById(id)
            );

            assertEquals(
                    String.format("A categoria do ID: %s não foi encontrado.", id),
                    exception.getMessage()
            );

            verify(repository, never()).deleteById(any());
        }
    }
}
