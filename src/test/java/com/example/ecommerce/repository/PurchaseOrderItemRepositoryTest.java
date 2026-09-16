package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Category;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.PurchaseOrder;
import com.example.ecommerce.entity.PurchaseOrderItem;
import com.example.ecommerce.entity.PurchaseOrderStatus;
import com.example.ecommerce.entity.Supplier;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class PurchaseOrderItemRepositoryTest {

    @Autowired
    private PurchaseOrderItemRepository purchaseOrderItemRepository;

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Supplier createSupplier() {

        Supplier supplier = new Supplier();

        supplier.setName("ABC Electronics");
        supplier.setEmail("supplier@gmail.com");
        supplier.setPhone("9876543210");
        supplier.setAddress("Chennai");

        return supplierRepository.save(supplier);
    }

    private Product createProduct(String name) {

        Category category = new Category();
        category.setName("Electronics");
        category = categoryRepository.save(category);

        Product product = new Product();
        product.setName(name);
        product.setPrice(50000.0);
        product.setDescription("Test product");
        product.setCategory(category);

        return productRepository.save(product);
    }

    private PurchaseOrder createPurchaseOrder() {

        Supplier supplier = createSupplier();

        PurchaseOrder purchaseOrder = new PurchaseOrder();

        purchaseOrder.setSupplier(supplier);
        purchaseOrder.setStatus(PurchaseOrderStatus.CREATED);
        purchaseOrder.setTotalAmount(
                BigDecimal.valueOf(50000.0)
        );

        return purchaseOrderRepository.save(purchaseOrder);
    }

    private PurchaseOrderItem createItem(
            PurchaseOrder purchaseOrder,
            Product product,
            int quantity,
            BigDecimal purchasePrice) {

        PurchaseOrderItem item =
                new PurchaseOrderItem();

        item.setPurchaseOrder(purchaseOrder);
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setPurchasePrice(purchasePrice);

        return purchaseOrderItemRepository.save(item);
    }

    @Test
    void savePurchaseOrderItem_success() {

        PurchaseOrder purchaseOrder =
                createPurchaseOrder();

        Product product =
                createProduct("Laptop");

        PurchaseOrderItem item =
                createItem(
                        purchaseOrder,
                        product,
                        5,
                        BigDecimal.valueOf(40000.0)
                );

        assertNotNull(item.getId());
        assertEquals(5, item.getQuantity());
        assertEquals(
                BigDecimal.valueOf(40000.0),
                item.getPurchasePrice()
        );
        assertEquals(
                product.getId(),
                item.getProduct().getId()
        );
        assertEquals(
                purchaseOrder.getId(),
                item.getPurchaseOrder().getId()
        );
    }

    @Test
    void findById_success() {

        PurchaseOrder purchaseOrder =
                createPurchaseOrder();

        Product product =
                createProduct("Laptop");

        PurchaseOrderItem item =
                createItem(
                        purchaseOrder,
                        product,
                        5,
                        BigDecimal.valueOf(40000.0)
                );

        Optional<PurchaseOrderItem> result =
                purchaseOrderItemRepository.findById(
                        item.getId()
                );

        assertTrue(result.isPresent());
        assertEquals(
                item.getId(),
                result.get().getId()
        );
        assertEquals(
                5,
                result.get().getQuantity()
        );
    }

    @Test
    void findById_notFound() {

        Optional<PurchaseOrderItem> result =
                purchaseOrderItemRepository.findById(999999L);

        assertTrue(result.isEmpty());
    }

    @Test
    void findAll_success() {

        PurchaseOrder purchaseOrder =
                createPurchaseOrder();

        Product product1 =
                createProduct("Laptop");

        Product product2 =
                createProduct("Phone");

        createItem(
                purchaseOrder,
                product1,
                2,
                BigDecimal.valueOf(40000.0)
        );

        createItem(
                purchaseOrder,
                product2,
                3,
                BigDecimal.valueOf(20000.0)
        );

        List<PurchaseOrderItem> result =
                purchaseOrderItemRepository.findAll();

        assertEquals(2, result.size());
    }

    @Test
    void findAll_empty() {

        List<PurchaseOrderItem> result =
                purchaseOrderItemRepository.findAll();

        assertTrue(result.isEmpty());
    }

    @Test
    void updatePurchaseOrderItem_success() {

        PurchaseOrder purchaseOrder =
                createPurchaseOrder();

        Product product =
                createProduct("Laptop");

        PurchaseOrderItem item =
                createItem(
                        purchaseOrder,
                        product,
                        5,
                        BigDecimal.valueOf(40000.0)
                );

        item.setQuantity(10);
        item.setPurchasePrice(
                BigDecimal.valueOf(38000.0)
        );

        PurchaseOrderItem updated =
                purchaseOrderItemRepository.save(item);

        assertEquals(10, updated.getQuantity());
        assertEquals(
                BigDecimal.valueOf(38000.0),
                updated.getPurchasePrice()
        );
    }

    @Test
    void deletePurchaseOrderItem_success() {

        PurchaseOrder purchaseOrder =
                createPurchaseOrder();

        Product product =
                createProduct("Laptop");

        PurchaseOrderItem item =
                createItem(
                        purchaseOrder,
                        product,
                        5,
                        BigDecimal.valueOf(40000.0)
                );

        Long id = item.getId();

        purchaseOrderItemRepository.delete(item);

        Optional<PurchaseOrderItem> result =
                purchaseOrderItemRepository.findById(id);

        assertTrue(result.isEmpty());
    }
}