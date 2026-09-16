package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Category;
import com.example.ecommerce.entity.Inventory;
import com.example.ecommerce.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class InventoryRepositoryTest {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Product createProduct(String name, double price) {

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

    @Test
    void saveInventory_success() {

        Product product =
                createProduct("Laptop", 50000.0);

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setQuantity(10);
        inventory.setLowStockThreshold(5);

        Inventory saved =
                inventoryRepository.save(inventory);

        assertNotNull(saved.getId());

        assertEquals(
                10,
                saved.getQuantity()
        );

        assertEquals(
                5,
                saved.getLowStockThreshold()
        );

        assertEquals(
                product.getId(),
                saved.getProduct().getId()
        );
    }

    @Test
    void findByProductId_success() {

        Product product =
                createProduct("Laptop", 50000.0);

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setQuantity(20);
        inventory.setLowStockThreshold(5);

        inventoryRepository.save(inventory);

        Optional<Inventory> result =
                inventoryRepository.findByProductId(
                        product.getId()
                );

        assertTrue(result.isPresent());

        assertEquals(
                20,
                result.get().getQuantity()
        );

        assertEquals(
                product.getId(),
                result.get().getProduct().getId()
        );
    }

    @Test
    void findByProductId_notFound() {

        Optional<Inventory> result =
                inventoryRepository.findByProductId(999999L);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByProductId_wrongProduct_returnsEmpty() {

        Product product1 =
                createProduct("Laptop", 50000.0);

        Product product2 =
                createProduct("Mouse", 1000.0);

        Inventory inventory = new Inventory();
        inventory.setProduct(product1);
        inventory.setQuantity(10);
        inventory.setLowStockThreshold(5);

        inventoryRepository.save(inventory);

        Optional<Inventory> result =
                inventoryRepository.findByProductId(
                        product2.getId()
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void updateInventory_success() {

        Product product =
                createProduct("Laptop", 50000.0);

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setQuantity(10);
        inventory.setLowStockThreshold(5);

        Inventory saved =
                inventoryRepository.save(inventory);

        saved.setQuantity(25);

        inventoryRepository.save(saved);

        Inventory updated =
                inventoryRepository.findById(saved.getId())
                        .orElseThrow();

        assertEquals(
                25,
                updated.getQuantity()
        );
    }

    @Test
    void deleteInventory_success() {

        Product product =
                createProduct("Laptop", 50000.0);

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setQuantity(10);
        inventory.setLowStockThreshold(5);

        Inventory saved =
                inventoryRepository.save(inventory);

        Long id = saved.getId();

        inventoryRepository.deleteById(id);

        assertTrue(
                inventoryRepository.findById(id).isEmpty()
        );
    }
}