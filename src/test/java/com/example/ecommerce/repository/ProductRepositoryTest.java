package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Category;
import com.example.ecommerce.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category electronics;
    private Category mobiles;

    @BeforeEach
    void setUp() {

        electronics = new Category();
        electronics.setName("Electronics");
        electronics = categoryRepository.save(electronics);

        mobiles = new Category();
        mobiles.setName("Mobiles");
        mobiles = categoryRepository.save(mobiles);

        Product laptop = new Product();
        laptop.setName("Dell Laptop");
        laptop.setPrice(50000.0);
        laptop.setDescription("Gaming laptop");
        laptop.setCategory(electronics);

        Product phone = new Product();
        phone.setName("Samsung Phone");
        phone.setPrice(25000.0);
        phone.setDescription("Android smartphone");
        phone.setCategory(mobiles);

        Product headphones = new Product();
        headphones.setName("Sony Headphones");
        headphones.setPrice(5000.0);
        headphones.setDescription("Wireless headphones");
        headphones.setCategory(electronics);

        productRepository.save(laptop);
        productRepository.save(phone);
        productRepository.save(headphones);
    }

    @Test
    void findByNameContainingIgnoreCase_success() {

        Page<Product> result =
                productRepository.findByNameContainingIgnoreCase(
                        "laptop",
                        PageRequest.of(0, 10)
                );

        assertEquals(1, result.getTotalElements());

        assertEquals(
                "Dell Laptop",
                result.getContent().getFirst().getName()
        );
    }

    @Test
    void findByNameContainingIgnoreCase_caseInsensitive() {

        Page<Product> result =
                productRepository.findByNameContainingIgnoreCase(
                        "LAPTOP",
                        PageRequest.of(0, 10)
                );

        assertEquals(1, result.getTotalElements());

        assertEquals(
                "Dell Laptop",
                result.getContent().getFirst().getName()
        );
    }

    @Test
    void findByNameContainingIgnoreCase_noResult() {

        Page<Product> result =
                productRepository.findByNameContainingIgnoreCase(
                        "Tablet",
                        PageRequest.of(0, 10)
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void findByCategoryNameIgnoreCase_success() {

        Page<Product> result =
                productRepository.findByCategoryNameIgnoreCase(
                        "electronics",
                        PageRequest.of(0, 10)
                );

        assertEquals(2, result.getTotalElements());
    }

    @Test
    void findByCategoryNameIgnoreCase_caseInsensitive() {

        Page<Product> result =
                productRepository.findByCategoryNameIgnoreCase(
                        "ELECTRONICS",
                        PageRequest.of(0, 10)
                );

        assertEquals(2, result.getTotalElements());
    }

    @Test
    void findByCategoryNameIgnoreCase_noResult() {

        Page<Product> result =
                productRepository.findByCategoryNameIgnoreCase(
                        "Furniture",
                        PageRequest.of(0, 10)
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void findByPriceBetween_success() {

        Page<Product> result =
                productRepository.findByPriceBetween(
                        BigDecimal.valueOf(10000),
                        BigDecimal.valueOf(60000),
                        PageRequest.of(0, 10)
                );

        assertEquals(2, result.getTotalElements());
    }

    @Test
    void findByPriceBetween_noResult() {

        Page<Product> result =
                productRepository.findByPriceBetween(
                        BigDecimal.valueOf(100000),
                        BigDecimal.valueOf(200000),
                        PageRequest.of(0, 10)
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void findByNameContainingIgnoreCaseAndPriceBetween_success() {

        Page<Product> result =
                productRepository
                        .findByNameContainingIgnoreCaseAndPriceBetween(
                                "laptop",
                                BigDecimal.valueOf(40000),
                                BigDecimal.valueOf(60000),
                                PageRequest.of(0, 10)
                        );

        assertEquals(1, result.getTotalElements());

        assertEquals(
                "Dell Laptop",
                result.getContent().getFirst().getName()
        );
    }

    @Test
    void findByNameContainingIgnoreCaseAndPriceBetween_noResult() {

        Page<Product> result =
                productRepository
                        .findByNameContainingIgnoreCaseAndPriceBetween(
                                "laptop",
                                BigDecimal.valueOf(1000),
                                BigDecimal.valueOf(10000),
                                PageRequest.of(0, 10)
                        );

        assertTrue(result.isEmpty());
    }

    @Test
    void pagination_shouldReturnCorrectPageSize() {

        Page<Product> result =
                productRepository.findAll(
                        PageRequest.of(0, 2)
                );

        assertEquals(2, result.getContent().size());
        assertEquals(3, result.getTotalElements());
    }

    @Test
    void saveAndFindById_success() {

        Product product = new Product();

        product.setName("Keyboard");
        product.setPrice(1500.0);
        product.setDescription("Mechanical keyboard");
        product.setCategory(electronics);

        Product saved =
                productRepository.save(product);

        assertNotNull(saved.getId());

        Product found =
                productRepository.findById(saved.getId())
                        .orElse(null);

        assertNotNull(found);

        assertEquals(
                "Keyboard",
                found.getName()
        );

        assertEquals(
                1500.0,
                found.getPrice()
        );
    }

    @Test
    void deleteById_success() {

        Product product = new Product();

        product.setName("Mouse");
        product.setPrice(800.0);
        product.setDescription("Wireless mouse");
        product.setCategory(electronics);

        Product saved =
                productRepository.save(product);

        Long id = saved.getId();

        productRepository.deleteById(id);

        assertFalse(
                productRepository.findById(id).isPresent()
        );
    }
}