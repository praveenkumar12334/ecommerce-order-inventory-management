package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Category;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.StockMovement;
import com.example.ecommerce.entity.StockMovementType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class StockMovementRepositoryTest {

    @Autowired
    private StockMovementRepository stockMovementRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Product createProduct(
            String name,
            double price) {

        Category category = new Category();
        category.setName("Electronics");
        category = categoryRepository.save(category);

        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setDescription("Test product");
        product.setCategory(category);

        return productRepository.save(product);
    }

    private StockMovement createMovement(
            Product product,
            int quantity,
            StockMovementType type,
            String note) {

        StockMovement movement = new StockMovement();

        movement.setProduct(product);
        movement.setQuantity(quantity);
        movement.setType(type);
        movement.setReferenceId(null);
        movement.setNote(note);

        return stockMovementRepository.save(movement);
    }

    @Test
    void saveMovement_success() {

        Product product =
                createProduct("Laptop", 50000.0);

        StockMovement movement =
                createMovement(
                        product,
                        10,
                        StockMovementType.PURCHASE_RECEIVED,
                        "Stock received"
                );

        assertNotNull(movement.getId());

        assertEquals(
                product.getId(),
                movement.getProduct().getId()
        );

        assertEquals(
                10,
                movement.getQuantity()
        );

        assertEquals(
                StockMovementType.PURCHASE_RECEIVED,
                movement.getType()
        );

        assertNotNull(
                movement.getCreatedAt()
        );
    }

    @Test
    void findByProductIdOrderByCreatedAtDesc_success() {

        Product product =
                createProduct("Laptop", 50000.0);

        createMovement(
                product,
                10,
                StockMovementType.PURCHASE_RECEIVED,
                "First movement"
        );

        StockMovement second =
                createMovement(
                        product,
                        2,
                        StockMovementType.ORDER_PLACED,
                        "Second movement"
                );

        List<StockMovement> result =
                stockMovementRepository
                        .findByProductIdOrderByCreatedAtDesc(
                                product.getId()
                        );

        assertEquals(2, result.size());

        assertEquals(
                product.getId(),
                result.getFirst().getProduct().getId()
        );

        assertEquals(
                second.getId(),
                result.getFirst().getId()
        );
    }

    @Test
    void findByProductIdOrderByCreatedAtDesc_onlyReturnsRequestedProduct() {

        Product laptop =
                createProduct("Laptop", 50000.0);

        Product mouse =
                createProduct("Mouse", 1000.0);

        createMovement(
                laptop,
                10,
                StockMovementType.PURCHASE_RECEIVED,
                "Laptop stock"
        );

        createMovement(
                mouse,
                5,
                StockMovementType.PURCHASE_RECEIVED,
                "Mouse stock"
        );

        List<StockMovement> result =
                stockMovementRepository
                        .findByProductIdOrderByCreatedAtDesc(
                                laptop.getId()
                        );

        assertEquals(1, result.size());

        assertEquals(
                laptop.getId(),
                result.getFirst().getProduct().getId()
        );
    }

    @Test
    void findByProductIdOrderByCreatedAtDesc_noResult() {

        List<StockMovement> result =
                stockMovementRepository
                        .findByProductIdOrderByCreatedAtDesc(
                                999999L
                        );

        assertTrue(result.isEmpty());
    }

    @Test
    void findAllByOrderByCreatedAtDesc_success() {

        Product product1 =
                createProduct("Laptop", 50000.0);

        Product product2 =
                createProduct("Mouse", 1000.0);

        createMovement(
                product1,
                10,
                StockMovementType.PURCHASE_RECEIVED,
                "First movement"
        );

        StockMovement second =
                createMovement(
                        product2,
                        2,
                        StockMovementType.ORDER_PLACED,
                        "Second movement"
                );

        List<StockMovement> result =
                stockMovementRepository
                        .findAllByOrderByCreatedAtDesc();

        assertEquals(2, result.size());

        assertEquals(
                second.getId(),
                result.getFirst().getId()
        );
    }

    @Test
    void findAllByOrderByCreatedAtDesc_empty() {

        List<StockMovement> result =
                stockMovementRepository
                        .findAllByOrderByCreatedAtDesc();

        assertTrue(result.isEmpty());
    }

    @Test
    void findById_success() {

        Product product =
                createProduct("Laptop", 50000.0);

        StockMovement movement =
                createMovement(
                        product,
                        5,
                        StockMovementType.MANUAL_ADJUSTMENT,
                        "Manual adjustment"
                );

        StockMovement found =
                stockMovementRepository
                        .findById(movement.getId())
                        .orElse(null);

        assertNotNull(found);

        assertEquals(
                movement.getId(),
                found.getId()
        );

        assertEquals(
                5,
                found.getQuantity()
        );
    }

    @Test
    void deleteMovement_success() {

        Product product =
                createProduct("Laptop", 50000.0);

        StockMovement movement =
                createMovement(
                        product,
                        5,
                        StockMovementType.MANUAL_ADJUSTMENT,
                        "Manual adjustment"
                );

        Long id = movement.getId();

        stockMovementRepository.deleteById(id);

        assertTrue(
                stockMovementRepository
                        .findById(id)
                        .isEmpty()
        );
    }
}