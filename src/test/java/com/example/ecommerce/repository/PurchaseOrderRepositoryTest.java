package com.example.ecommerce.repository;

import com.example.ecommerce.entity.PurchaseOrder;
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
class PurchaseOrderRepositoryTest {

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    private Supplier createSupplier() {

        Supplier supplier = new Supplier();

        supplier.setName("ABC Electronics");
        supplier.setEmail("supplier@gmail.com");
        supplier.setPhone("9876543210");
        supplier.setAddress("Chennai");

        return supplierRepository.save(supplier);
    }

    private PurchaseOrder createPurchaseOrder() {

        Supplier supplier = createSupplier();

        PurchaseOrder purchaseOrder = new PurchaseOrder();

        purchaseOrder.setSupplier(supplier);
        purchaseOrder.setStatus(PurchaseOrderStatus.CREATED);
        purchaseOrder.setTotalAmount(BigDecimal.valueOf(50000.0));

        return purchaseOrderRepository.save(purchaseOrder);
    }

    @Test
    void savePurchaseOrder_success() {

        PurchaseOrder purchaseOrder = createPurchaseOrder();

        assertNotNull(purchaseOrder.getId());
        assertEquals(
                PurchaseOrderStatus.CREATED,
                purchaseOrder.getStatus()
        );
        assertEquals(
                BigDecimal.valueOf(50000.0),
                purchaseOrder.getTotalAmount()
        );
    }

    @Test
    void findById_success() {

        PurchaseOrder saved = createPurchaseOrder();

        Optional<PurchaseOrder> result =
                purchaseOrderRepository.findById(saved.getId());

        assertTrue(result.isPresent());
        assertEquals(saved.getId(), result.get().getId());
        assertEquals(
                PurchaseOrderStatus.CREATED,
                result.get().getStatus()
        );
    }

    @Test
    void findById_notFound() {

        Optional<PurchaseOrder> result =
                purchaseOrderRepository.findById(999999L);

        assertTrue(result.isEmpty());
    }

    @Test
    void findAll_success() {

        createPurchaseOrder();
        createPurchaseOrder();

        List<PurchaseOrder> result =
                purchaseOrderRepository.findAll();

        assertEquals(2, result.size());
    }

    @Test
    void findAll_empty() {

        List<PurchaseOrder> result =
                purchaseOrderRepository.findAll();

        assertTrue(result.isEmpty());
    }

    @Test
    void updatePurchaseOrder_success() {

        PurchaseOrder purchaseOrder =
                createPurchaseOrder();

        purchaseOrder.setStatus(PurchaseOrderStatus.ORDERED);

        PurchaseOrder updated =
                purchaseOrderRepository.save(purchaseOrder);

        assertEquals(
                PurchaseOrderStatus.ORDERED,
                updated.getStatus()
        );
    }

    @Test
    void deletePurchaseOrder_success() {

        PurchaseOrder purchaseOrder =
                createPurchaseOrder();

        Long id = purchaseOrder.getId();

        purchaseOrderRepository.delete(purchaseOrder);

        Optional<PurchaseOrder> result =
                purchaseOrderRepository.findById(id);

        assertTrue(result.isEmpty());
    }
}