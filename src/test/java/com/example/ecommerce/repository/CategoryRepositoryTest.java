package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void saveCategory_success() {

        Category category = new Category();
        category.setName("Electronics");

        Category saved =
                categoryRepository.save(category);

        assertNotNull(saved.getId());

        assertEquals(
                "Electronics",
                saved.getName()
        );
    }

    @Test
    void findById_success() {

        Category category = new Category();
        category.setName("Mobiles");

        Category saved =
                categoryRepository.save(category);

        Optional<Category> result =
                categoryRepository.findById(saved.getId());

        assertTrue(result.isPresent());

        assertEquals(
                "Mobiles",
                result.get().getName()
        );
    }

    @Test
    void findById_notFound() {

        Optional<Category> result =
                categoryRepository.findById(999999L);

        assertTrue(result.isEmpty());
    }

    @Test
    void findAll_success() {

        Category category1 = new Category();
        category1.setName("Electronics");

        Category category2 = new Category();
        category2.setName("Mobiles");

        categoryRepository.save(category1);
        categoryRepository.save(category2);

        List<Category> categories =
                categoryRepository.findAll();

        assertEquals(2, categories.size());
    }

    @Test
    void updateCategory_success() {

        Category category = new Category();
        category.setName("Electronic Items");

        Category saved =
                categoryRepository.save(category);

        saved.setName("Electronics");

        categoryRepository.save(saved);

        Category updated =
                categoryRepository.findById(saved.getId())
                        .orElseThrow();

        assertEquals(
                "Electronics",
                updated.getName()
        );
    }

    @Test
    void deleteCategory_success() {

        Category category = new Category();
        category.setName("Accessories");

        Category saved =
                categoryRepository.save(category);

        Long id = saved.getId();

        categoryRepository.deleteById(id);

        Optional<Category> result =
                categoryRepository.findById(id);

        assertTrue(result.isEmpty());
    }
}