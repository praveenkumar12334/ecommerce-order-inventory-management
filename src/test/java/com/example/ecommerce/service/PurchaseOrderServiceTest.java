package com.example.ecommerce.service;

import com.example.ecommerce.dto.PurchaseOrderItemRequest;
import com.example.ecommerce.dto.PurchaseOrderRequest;
import com.example.ecommerce.entity.*;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.InventoryRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.PurchaseOrderRepository;
import com.example.ecommerce.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseOrderServiceTest {

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private StockMovementService stockMovementService;

    @InjectMocks
    private PurchaseOrderService purchaseOrderService;

    private Supplier supplier;
    private Product product;
    private Inventory inventory;

    @BeforeEach
    void setUp() {

        supplier = new Supplier();
        supplier.setName("ABC Suppliers");
        supplier.setEmail("supplier@gmail.com");

        product = new Product();
        product.setId(1L);
        product.setName("Laptop");
        product.setPrice(50000.0);

        inventory = new Inventory();
        inventory.setQuantity(10);
    }

    private PurchaseOrderItemRequest createItemRequest(
            Long productId,
            int quantity,
            BigDecimal price) {

        PurchaseOrderItemRequest item =
                new PurchaseOrderItemRequest();

        item.setProductId(productId);
        item.setQuantity(quantity);
        item.setPurchasePrice(price);

        return item;
    }

    private PurchaseOrderRequest createRequest(
            Long supplierId,
            List<PurchaseOrderItemRequest> items) {

        PurchaseOrderRequest request =
                new PurchaseOrderRequest();

        request.setSupplierId(supplierId);
        request.setItems(items);

        return request;
    }

    @Test
    void createPurchaseOrder_success() {

        PurchaseOrderRequest request =
                createRequest(
                        1L,
                        List.of(
                                createItemRequest(
                                        1L,
                                        2,
                                        new BigDecimal("40000")
                                )
                        )
                );

        when(supplierRepository.findById(1L))
                .thenReturn(Optional.of(supplier));

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(purchaseOrderRepository.save(any(PurchaseOrder.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        PurchaseOrder result =
                purchaseOrderService.createPurchaseOrder(request);

        assertNotNull(result);
        assertEquals(supplier, result.getSupplier());
        assertEquals(
                PurchaseOrderStatus.CREATED,
                result.getStatus()
        );
        assertEquals(
                new BigDecimal("80000"),
                result.getTotalAmount()
        );
        assertEquals(1, result.getItems().size());

        verify(purchaseOrderRepository)
                .save(any(PurchaseOrder.class));
    }

    @Test
    void createPurchaseOrder_supplierNotFound() {

        PurchaseOrderRequest request =
                createRequest(
                        1L,
                        List.of(
                                createItemRequest(
                                        1L,
                                        2,
                                        new BigDecimal("40000")
                                )
                        )
                );

        when(supplierRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> purchaseOrderService
                        .createPurchaseOrder(request)
        );

        verify(purchaseOrderRepository, never())
                .save(any());
    }

    @Test
    void createPurchaseOrder_productNotFound() {

        PurchaseOrderRequest request =
                createRequest(
                        1L,
                        List.of(
                                createItemRequest(
                                        1L,
                                        2,
                                        new BigDecimal("40000")
                                )
                        )
                );

        when(supplierRepository.findById(1L))
                .thenReturn(Optional.of(supplier));

        when(productRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> purchaseOrderService
                        .createPurchaseOrder(request)
        );

        verify(purchaseOrderRepository, never())
                .save(any());
    }

    @Test
    void createPurchaseOrder_zeroQuantity_rejected() {

        PurchaseOrderRequest request =
                createRequest(
                        1L,
                        List.of(
                                createItemRequest(
                                        1L,
                                        0,
                                        new BigDecimal("40000")
                                )
                        )
                );

        when(supplierRepository.findById(1L))
                .thenReturn(Optional.of(supplier));

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        assertThrows(
                IllegalArgumentException.class,
                () -> purchaseOrderService
                        .createPurchaseOrder(request)
        );

        verify(purchaseOrderRepository, never())
                .save(any());
    }

    @Test
    void createPurchaseOrder_negativeQuantity_rejected() {

        PurchaseOrderRequest request =
                createRequest(
                        1L,
                        List.of(
                                createItemRequest(
                                        1L,
                                        -2,
                                        new BigDecimal("40000")
                                )
                        )
                );

        when(supplierRepository.findById(1L))
                .thenReturn(Optional.of(supplier));

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        assertThrows(
                IllegalArgumentException.class,
                () -> purchaseOrderService
                        .createPurchaseOrder(request)
        );
    }

    @Test
    void createPurchaseOrder_multipleItems_totalCalculated() {

        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Mouse");
        product2.setPrice(1000.0);

        PurchaseOrderRequest request =
                createRequest(
                        1L,
                        List.of(
                                createItemRequest(
                                        1L,
                                        2,
                                        new BigDecimal("40000")
                                ),
                                createItemRequest(
                                        2L,
                                        5,
                                        new BigDecimal("1000")
                                )
                        )
                );

        when(supplierRepository.findById(1L))
                .thenReturn(Optional.of(supplier));

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(productRepository.findById(2L))
                .thenReturn(Optional.of(product2));

        when(purchaseOrderRepository.save(any(PurchaseOrder.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        PurchaseOrder result =
                purchaseOrderService.createPurchaseOrder(request);

        assertEquals(2, result.getItems().size());

        assertEquals(
                new BigDecimal("85000"),
                result.getTotalAmount()
        );
    }

    @Test
    void getAllPurchaseOrders_success() {

        PurchaseOrder order1 = new PurchaseOrder();
        PurchaseOrder order2 = new PurchaseOrder();

        when(purchaseOrderRepository.findAll())
                .thenReturn(List.of(order1, order2));

        List<PurchaseOrder> result =
                purchaseOrderService.getAllPurchaseOrders();

        assertEquals(2, result.size());

        verify(purchaseOrderRepository)
                .findAll();
    }

    @Test
    void getPurchaseOrderById_success() {

        PurchaseOrder order =
                new PurchaseOrder();

        when(purchaseOrderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        PurchaseOrder result =
                purchaseOrderService
                        .getPurchaseOrderById(1L);

        assertNotNull(result);
        assertEquals(order, result);
    }

    @Test
    void getPurchaseOrderById_notFound() {

        when(purchaseOrderRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> purchaseOrderService
                        .getPurchaseOrderById(1L)
        );
    }

    @Test
    void markAsOrdered_success() {

        PurchaseOrder order =
                new PurchaseOrder();

        order.setStatus(PurchaseOrderStatus.CREATED);

        when(purchaseOrderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(purchaseOrderRepository.save(any(PurchaseOrder.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        PurchaseOrder result =
                purchaseOrderService.markAsOrdered(1L);

        assertEquals(
                PurchaseOrderStatus.ORDERED,
                result.getStatus()
        );

        verify(purchaseOrderRepository)
                .save(order);
    }

    @Test
    void markAsOrdered_alreadyOrdered_rejected() {

        PurchaseOrder order =
                new PurchaseOrder();

        order.setStatus(PurchaseOrderStatus.ORDERED);

        when(purchaseOrderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        assertThrows(
                IllegalArgumentException.class,
                () -> purchaseOrderService
                        .markAsOrdered(1L)
        );

        verify(purchaseOrderRepository, never())
                .save(any());
    }

    @Test
    void markAsOrdered_received_rejected() {

        PurchaseOrder order =
                new PurchaseOrder();

        order.setStatus(PurchaseOrderStatus.RECEIVED);

        when(purchaseOrderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        assertThrows(
                IllegalArgumentException.class,
                () -> purchaseOrderService
                        .markAsOrdered(1L)
        );
    }

    @Test
    void receivePurchaseOrder_success() {

        PurchaseOrderItem item =
                new PurchaseOrderItem();

        item.setProduct(product);
        item.setQuantity(5);

        PurchaseOrder order =
                new PurchaseOrder();

        order.setId(1L);
        order.setStatus(PurchaseOrderStatus.ORDERED);
        order.setItems(
                new ArrayList<>(List.of(item))
        );

        when(purchaseOrderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(inventoryRepository.findByProductId(1L))
                .thenReturn(Optional.of(inventory));

        when(inventoryRepository.save(any(Inventory.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        when(purchaseOrderRepository.save(any(PurchaseOrder.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        PurchaseOrder result =
                purchaseOrderService
                        .receivePurchaseOrder(1L);

        assertEquals(15, inventory.getQuantity());

        assertEquals(
                PurchaseOrderStatus.RECEIVED,
                result.getStatus()
        );

        verify(inventoryRepository)
                .save(inventory);

        verify(stockMovementService)
                .recordMovement(
                        1L,
                        5,
                        StockMovementType.PURCHASE_RECEIVED,
                        1L,
                        "Stock received from purchase order"
                );

        verify(purchaseOrderRepository)
                .save(order);
    }

    @Test
    void receivePurchaseOrder_inventoryNotFound_createsInventory()
            throws Exception {

        PurchaseOrderItem item =
                new PurchaseOrderItem();

        item.setProduct(product);
        item.setQuantity(5);

        PurchaseOrder order =
                new PurchaseOrder();

        order.setId(1L);
        order.setStatus(PurchaseOrderStatus.ORDERED);
        order.setItems(
                new ArrayList<>(List.of(item))
        );

        when(purchaseOrderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(inventoryRepository.findByProductId(1L))
                .thenReturn(Optional.empty());

        when(inventoryRepository.save(any(Inventory.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        when(purchaseOrderRepository.save(any(PurchaseOrder.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        PurchaseOrder result =
                purchaseOrderService
                        .receivePurchaseOrder(1L);

        assertEquals(
                PurchaseOrderStatus.RECEIVED,
                result.getStatus()
        );

        verify(inventoryRepository)
                .save(any(Inventory.class));

        verify(stockMovementService)
                .recordMovement(
                        eq(1L),
                        eq(5),
                        eq(StockMovementType.PURCHASE_RECEIVED),
                        eq(1L),
                        eq("Stock received from purchase order")
                );

        verify(purchaseOrderRepository)
                .save(order);
    }

    @Test
    void receivePurchaseOrder_createdStatus_rejected() {

        PurchaseOrder order =
                new PurchaseOrder();

        order.setStatus(PurchaseOrderStatus.CREATED);

        when(purchaseOrderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        assertThrows(
                IllegalArgumentException.class,
                () -> purchaseOrderService
                        .receivePurchaseOrder(1L)
        );

        verify(inventoryRepository, never())
                .save(any());
    }

    @Test
    void receivePurchaseOrder_receivedStatus_rejected() {

        PurchaseOrder order =
                new PurchaseOrder();

        order.setStatus(PurchaseOrderStatus.RECEIVED);

        when(purchaseOrderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        assertThrows(
                IllegalArgumentException.class,
                () -> purchaseOrderService
                        .receivePurchaseOrder(1L)
        );
    }

    @Test
    void receivePurchaseOrder_multipleItems_updatesInventory() {

        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Keyboard");

        Inventory inventory2 = new Inventory();
        inventory2.setQuantity(20);

        PurchaseOrderItem item1 =
                new PurchaseOrderItem();

        item1.setProduct(product);
        item1.setQuantity(5);

        PurchaseOrderItem item2 =
                new PurchaseOrderItem();

        item2.setProduct(product2);
        item2.setQuantity(10);

        PurchaseOrder order =
                new PurchaseOrder();

        order.setId(1L);
        order.setStatus(PurchaseOrderStatus.ORDERED);
        order.setItems(
                new ArrayList<>(
                        List.of(item1, item2)
                )
        );

        when(purchaseOrderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(inventoryRepository.findByProductId(1L))
                .thenReturn(Optional.of(inventory));

        when(inventoryRepository.findByProductId(2L))
                .thenReturn(Optional.of(inventory2));

        when(inventoryRepository.save(any(Inventory.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        when(purchaseOrderRepository.save(any(PurchaseOrder.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        PurchaseOrder result =
                purchaseOrderService
                        .receivePurchaseOrder(1L);

        assertEquals(15, inventory.getQuantity());
        assertEquals(30, inventory2.getQuantity());

        assertEquals(
                PurchaseOrderStatus.RECEIVED,
                result.getStatus()
        );

        verify(stockMovementService, times(2))
                .recordMovement(
                        anyLong(),
                        anyInt(),
                        eq(StockMovementType.PURCHASE_RECEIVED),
                        eq(1L),
                        eq("Stock received from purchase order")
                );
    }
}