package com.example.ecommerce.service;

import com.example.ecommerce.entity.Category;
import com.example.ecommerce.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;

    @BeforeEach
    void setUp() {

        category = new Category();
        category.setId(1L);
        category.setName("Electronics");
    }

    // =========================================================
    // saveCategory()
    // =========================================================

    @Test
    void saveCategory_shouldSaveSuccessfully() {

        when(categoryRepository.save(category))
                .thenReturn(category);

        Category result =
                categoryService.saveCategory(category);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Electronics", result.getName());

        verify(categoryRepository)
                .save(category);
    }

    // =========================================================
    // getAllCategories()
    // =========================================================

    @Test
    void getAllCategories_shouldReturnAllCategories() {

        Category category2 = new Category();
        category2.setId(2L);
        category2.setName("Clothing");

        when(categoryRepository.findAll())
                .thenReturn(List.of(category, category2));

        List<Category> result =
                categoryService.getAllCategories();

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(
                "Electronics",
                result.get(0).getName()
        );

        assertEquals(
                "Clothing",
                result.get(1).getName()
        );

        verify(categoryRepository)
                .findAll();
    }

    @Test
    void getAllCategories_shouldReturnEmptyListWhenNoCategories() {

        when(categoryRepository.findAll())
                .thenReturn(List.of());

        List<Category> result =
                categoryService.getAllCategories();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(categoryRepository)
                .findAll();
    }
}