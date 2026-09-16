package com.example.ecommerce.service;

import com.example.ecommerce.dto.OrderItemRequest;
import com.example.ecommerce.dto.OrderRequest;
import com.example.ecommerce.dto.OrderResponse;
import com.example.ecommerce.entity.*;
import com.example.ecommerce.exception.AccessDeniedException;
import com.example.ecommerce.exception.InsufficientStockException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.InventoryRepository;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.ProductRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private StockMovementService stockMovementService;

    @Mock
    private AddressService addressService;

    @InjectMocks
    private OrderService orderService;


    // =========================================================
    // TEST DATA
    // =========================================================

    private Product product;
    private Inventory inventory;
    private Address address;


    @BeforeEach
    void setUp() {

        product = new Product();
        product.setId(1L);
        product.setName("Laptop");
        product.setPrice(50000.0);

        inventory = new Inventory();
        inventory.setQuantity(10);

        address = new Address();
        address.setAddressLine("123 Main Street");
        address.setCity("Salem");
        address.setState("Tamil Nadu");
        address.setPincode("636001");
    }


    // =========================================================
    // 1. CREATE ORDER SUCCESSFULLY
    // =========================================================

    @Test
    void saveOrderSuccessfully() {

        OrderRequest request = new OrderRequest();
        request.setCustomerName("Praveen");
        request.setAddressId(1L);

        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(1L);
        item.setQuantity(2);

        request.setItems(List.of(item));

        when(addressService.getMyAddress(
                1L,
                "user@gmail.com"
        )).thenReturn(address);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(inventoryRepository.findByProductId(1L))
                .thenReturn(Optional.of(inventory));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response =
                orderService.saveOrder(
                        request,
                        "user@gmail.com"
                );

        assertNotNull(response);

        assertEquals(
                100000.0,
                response.getTotalAmount()
        );

        assertEquals(
                8,
                inventory.getQuantity()
        );

        verify(inventoryRepository)
                .save(inventory);

        verify(orderRepository)
                .save(any(Order.class));

        verify(stockMovementService)
                .recordMovement(
                        eq(1L),
                        eq(2),
                        eq(StockMovementType.ORDER_PLACED),
                        any(),
                        eq("Stock reduced for customer order")
                );
    }


    // =========================================================
    // 2. EMPTY ORDER SHOULD FAIL
    // =========================================================

    @Test
    void saveOrderWithEmptyItemsShouldFail() {

        OrderRequest request = new OrderRequest();

        request.setCustomerName("Praveen");
        request.setAddressId(1L);
        request.setItems(new ArrayList<>());

        when(addressService.getMyAddress(
                1L,
                "user@gmail.com"
        )).thenReturn(address);

        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.saveOrder(
                        request,
                        "user@gmail.com"
                )
        );

        verifyNoInteractions(productRepository);
        verifyNoInteractions(inventoryRepository);
        verifyNoInteractions(orderRepository);
    }


    // =========================================================
    // 3. NULL ITEMS SHOULD FAIL
    // =========================================================

    @Test
    void saveOrderWithNullItemsShouldFail() {

        OrderRequest request = new OrderRequest();

        request.setCustomerName("Praveen");
        request.setAddressId(1L);
        request.setItems(null);

        when(addressService.getMyAddress(
                1L,
                "user@gmail.com"
        )).thenReturn(address);

        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.saveOrder(
                        request,
                        "user@gmail.com"
                )
        );

        verifyNoInteractions(productRepository);
        verifyNoInteractions(inventoryRepository);
    }


    // =========================================================
    // 4. ZERO QUANTITY SHOULD FAIL
    // =========================================================

    @Test
    void zeroQuantityShouldFail() {

        OrderRequest request = new OrderRequest();

        request.setCustomerName("Praveen");
        request.setAddressId(1L);

        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(1L);
        item.setQuantity(0);

        request.setItems(List.of(item));

        when(addressService.getMyAddress(
                1L,
                "user@gmail.com"
        )).thenReturn(address);

        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.saveOrder(
                        request,
                        "user@gmail.com"
                )
        );

        verifyNoInteractions(productRepository);
    }


    // =========================================================
    // 5. NEGATIVE QUANTITY SHOULD FAIL
    // =========================================================

    @Test
    void negativeQuantityShouldFail() {

        OrderRequest request = new OrderRequest();

        request.setCustomerName("Praveen");
        request.setAddressId(1L);

        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(1L);
        item.setQuantity(-2);

        request.setItems(List.of(item));

        when(addressService.getMyAddress(
                1L,
                "user@gmail.com"
        )).thenReturn(address);

        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.saveOrder(
                        request,
                        "user@gmail.com"
                )
        );

        verifyNoInteractions(productRepository);
    }


    // =========================================================
    // 6. PRODUCT NOT FOUND
    // =========================================================

    @Test
    void productNotFoundShouldFail() {

        OrderRequest request = new OrderRequest();

        request.setCustomerName("Praveen");
        request.setAddressId(1L);

        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(99L);
        item.setQuantity(2);

        request.setItems(List.of(item));

        when(addressService.getMyAddress(
                1L,
                "user@gmail.com"
        )).thenReturn(address);

        when(productRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.saveOrder(
                        request,
                        "user@gmail.com"
                )
        );

        verify(inventoryRepository, never())
                .findByProductId(anyLong());

        verify(orderRepository, never())
                .save(any());
    }


    // =========================================================
    // 7. INVENTORY NOT FOUND
    // =========================================================

    @Test
    void inventoryNotFoundShouldFail() {

        OrderRequest request = new OrderRequest();

        request.setCustomerName("Praveen");
        request.setAddressId(1L);

        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(1L);
        item.setQuantity(2);

        request.setItems(List.of(item));

        when(addressService.getMyAddress(
                1L,
                "user@gmail.com"
        )).thenReturn(address);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(inventoryRepository.findByProductId(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.saveOrder(
                        request,
                        "user@gmail.com"
                )
        );

        verify(orderRepository, never())
                .save(any());
    }


    // =========================================================
    // 8. INSUFFICIENT STOCK
    // =========================================================

    @Test
    void insufficientStockShouldFail() {

        OrderRequest request = new OrderRequest();

        request.setCustomerName("Praveen");
        request.setAddressId(1L);

        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(1L);
        item.setQuantity(20);

        request.setItems(List.of(item));

        when(addressService.getMyAddress(
                1L,
                "user@gmail.com"
        )).thenReturn(address);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(inventoryRepository.findByProductId(1L))
                .thenReturn(Optional.of(inventory));

        assertThrows(
                InsufficientStockException.class,
                () -> orderService.saveOrder(
                        request,
                        "user@gmail.com"
                )
        );

        assertEquals(
                10,
                inventory.getQuantity()
        );

        verify(orderRepository, never())
                .save(any());

        verify(stockMovementService, never())
                .recordMovement(
                        anyLong(),
                        anyInt(),
                        any(),
                        any(),
                        anyString()
                );
    }


    // =========================================================
    // 9. GET ALL ORDERS
    // =========================================================

    @Test
    void getAllOrdersSuccessfully() {

        Order order = createOrder(
                1L,
                "user@gmail.com",
                OrderStatus.PLACED,
                50000.0
        );

        when(orderRepository.findAll())
                .thenReturn(List.of(order));

        List<OrderResponse> responses =
                orderService.getAllOrders();

        assertNotNull(responses);
        assertEquals(1, responses.size());

        verify(orderRepository)
                .findAll();
    }


    // =========================================================
    // 10. GET ORDER BY ID
    // =========================================================

    @Test
    void getOrderByIdSuccessfully() {

        Order order = createOrder(
                1L,
                "user@gmail.com",
                OrderStatus.PLACED,
                50000.0
        );

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        OrderResponse response =
                orderService.getOrderById(1L);

        assertNotNull(response);

        assertEquals(
                OrderStatus.PLACED,
                response.getStatus()
        );

        assertEquals(
                50000.0,
                response.getTotalAmount()
        );

        verify(orderRepository)
                .findById(1L);
    }


    // =========================================================
    // 11. ORDER NOT FOUND
    // =========================================================

    @Test
    void getOrderByIdWhenOrderDoesNotExistShouldFail() {

        when(orderRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.getOrderById(99L)
        );

        verify(orderRepository)
                .findById(99L);
    }


    // =========================================================
    // 12. GET MY ORDERS
    // =========================================================

    @Test
    void getMyOrdersSuccessfully() {

        Order order1 = createOrder(
                1L,
                "user@gmail.com",
                OrderStatus.PLACED,
                50000.0
        );

        Order order2 = createOrder(
                2L,
                "user@gmail.com",
                OrderStatus.CONFIRMED,
                60000.0
        );

        when(orderRepository.findByCustomerEmail(
                "user@gmail.com"
        )).thenReturn(
                List.of(order1, order2)
        );

        List<OrderResponse> responses =
                orderService.getMyOrders(
                        "user@gmail.com"
                );

        assertEquals(
                2,
                responses.size()
        );

        verify(orderRepository)
                .findByCustomerEmail(
                        "user@gmail.com"
                );
    }


    // =========================================================
    // 13. CUSTOMER CAN ACCESS OWN ORDER
    // =========================================================

    @Test
    void customerCanAccessOwnOrder() {

        Order order = createOrder(
                1L,
                "user@gmail.com",
                OrderStatus.PLACED,
                50000.0
        );

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        OrderResponse response =
                orderService.getMyOrderById(
                        1L,
                        "user@gmail.com"
                );

        assertNotNull(response);

        verify(orderRepository)
                .findById(1L);
    }


    // =========================================================
    // 14. CUSTOMER CANNOT ACCESS OTHER USER ORDER
    // =========================================================

    @Test
    void customerCannotAccessOtherUsersOrder() {

        Order order = createOrder(
                1L,
                "other@gmail.com",
                OrderStatus.PLACED,
                50000.0
        );

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        assertThrows(
                AccessDeniedException.class,
                () -> orderService.getMyOrderById(
                        1L,
                        "user@gmail.com"
                )
        );

        verify(orderRepository)
                .findById(1L);
    }


    // =========================================================
    // 15. CANCEL ORDER SUCCESSFULLY
    // =========================================================

    @Test
    void cancelOrderSuccessfully() {

        Order order = createOrder(
                1L,
                "user@gmail.com",
                OrderStatus.PLACED,
                50000.0
        );

        Product orderProduct = new Product();
        orderProduct.setId(1L);
        orderProduct.setName("Laptop");
        orderProduct.setPrice(50000.0);

        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(orderProduct);
        orderItem.setQuantity(2);
        orderItem.setOrder(order);

        order.setOrderItems(
                new ArrayList<>(
                        List.of(orderItem)
                )
        );

        Inventory orderInventory = new Inventory();
        orderInventory.setQuantity(5);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(inventoryRepository.findByProductId(1L))
                .thenReturn(Optional.of(orderInventory));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        OrderResponse response =
                orderService.cancelMyOrder(
                        1L,
                        "user@gmail.com"
                );

        assertNotNull(response);

        assertEquals(
                OrderStatus.CANCELLED,
                response.getStatus()
        );

        assertEquals(
                7,
                orderInventory.getQuantity()
        );

        verify(inventoryRepository)
                .save(orderInventory);

        verify(stockMovementService)
                .recordMovement(
                        eq(1L),
                        eq(2),
                        eq(StockMovementType.ORDER_CANCELLED),
                        eq(1L),
                        eq("Stock restored after order cancellation")
                );

        verify(orderRepository)
                .save(order);
    }


    // =========================================================
    // 16. CANNOT CANCEL SOMEONE ELSE'S ORDER
    // =========================================================

    @Test
    void cannotCancelOtherUsersOrder() {

        Order order = createOrder(
                1L,
                "other@gmail.com",
                OrderStatus.PLACED,
                50000.0
        );

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        assertThrows(
                AccessDeniedException.class,
                () -> orderService.cancelMyOrder(
                        1L,
                        "user@gmail.com"
                )
        );

        verify(inventoryRepository, never())
                .findByProductId(anyLong());

        verify(orderRepository, never())
                .save(any());
    }


    // =========================================================
    // 17. SHIPPED ORDER CANNOT BE CANCELLED
    // =========================================================

    @Test
    void shippedOrderCannotBeCancelled() {

        Order order = createOrder(
                1L,
                "user@gmail.com",
                OrderStatus.SHIPPED,
                50000.0
        );

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        assertThrows(
                IllegalStateException.class,
                () -> orderService.cancelMyOrder(
                        1L,
                        "user@gmail.com"
                )
        );

        verify(inventoryRepository, never())
                .findByProductId(anyLong());

        verify(orderRepository, never())
                .save(any());
    }


    // =========================================================
    // 18. DELIVERED ORDER CANNOT BE CANCELLED
    // =========================================================

    @Test
    void deliveredOrderCannotBeCancelled() {

        Order order = createOrder(
                1L,
                "user@gmail.com",
                OrderStatus.DELIVERED,
                50000.0
        );

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        assertThrows(
                IllegalStateException.class,
                () -> orderService.cancelMyOrder(
                        1L,
                        "user@gmail.com"
                )
        );

        verify(inventoryRepository, never())
                .findByProductId(anyLong());
    }


    // =========================================================
    // 19. ALREADY CANCELLED ORDER CANNOT BE CANCELLED
    // =========================================================

    @Test
    void alreadyCancelledOrderCannotBeCancelled() {

        Order order = createOrder(
                1L,
                "user@gmail.com",
                OrderStatus.CANCELLED,
                50000.0
        );

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        assertThrows(
                IllegalStateException.class,
                () -> orderService.cancelMyOrder(
                        1L,
                        "user@gmail.com"
                )
        );

        verify(inventoryRepository, never())
                .findByProductId(anyLong());
    }


    // =========================================================
    // 20. PLACED → CONFIRMED
    // =========================================================

    @Test
    void placedOrderCanBeConfirmed() {

        Order order = createOrder(
                1L,
                "user@gmail.com",
                OrderStatus.PLACED,
                50000.0
        );

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        OrderResponse response =
                orderService.updateOrderStatus(
                        1L,
                        OrderStatus.CONFIRMED
                );

        assertEquals(
                OrderStatus.CONFIRMED,
                response.getStatus()
        );

        verify(orderRepository)
                .save(order);
    }


    // =========================================================
    // 21. CONFIRMED → SHIPPED
    // =========================================================

    @Test
    void confirmedOrderCanBeShipped() {

        Order order = createOrder(
                1L,
                "user@gmail.com",
                OrderStatus.CONFIRMED,
                50000.0
        );

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        OrderResponse response =
                orderService.updateOrderStatus(
                        1L,
                        OrderStatus.SHIPPED
                );

        assertEquals(
                OrderStatus.SHIPPED,
                response.getStatus()
        );

        verify(orderRepository)
                .save(order);
    }


    // =========================================================
    // 22. SHIPPED → DELIVERED
    // =========================================================

    @Test
    void shippedOrderCanBeDelivered() {

        Order order = createOrder(
                1L,
                "user@gmail.com",
                OrderStatus.SHIPPED,
                50000.0
        );

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        OrderResponse response =
                orderService.updateOrderStatus(
                        1L,
                        OrderStatus.DELIVERED
                );

        assertEquals(
                OrderStatus.DELIVERED,
                response.getStatus()
        );

        verify(orderRepository)
                .save(order);
    }


    // =========================================================
    // 23. INVALID STATUS TRANSITION
    // =========================================================

    @Test
    void invalidStatusTransitionShouldFail() {

        Order order = createOrder(
                1L,
                "user@gmail.com",
                OrderStatus.PLACED,
                50000.0
        );

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        assertThrows(
                IllegalStateException.class,
                () -> orderService.updateOrderStatus(
                        1L,
                        OrderStatus.SHIPPED
                )
        );

        verify(orderRepository, never())
                .save(any());
    }


    // =========================================================
    // 24. CANCELLED ORDER CANNOT CHANGE STATUS
    // =========================================================

    @Test
    void cancelledOrderCannotChangeStatus() {

        Order order = createOrder(
                1L,
                "user@gmail.com",
                OrderStatus.CANCELLED,
                50000.0
        );

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        assertThrows(
                IllegalStateException.class,
                () -> orderService.updateOrderStatus(
                        1L,
                        OrderStatus.CONFIRMED
                )
        );

        verify(orderRepository, never())
                .save(any());
    }


    // =========================================================
    // 25. DELIVERED ORDER CANNOT CHANGE STATUS
    // =========================================================

    @Test
    void deliveredOrderCannotChangeStatus() {

        Order order = createOrder(
                1L,
                "user@gmail.com",
                OrderStatus.DELIVERED,
                50000.0
        );

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        assertThrows(
                IllegalStateException.class,
                () -> orderService.updateOrderStatus(
                        1L,
                        OrderStatus.CONFIRMED
                )
        );

        verify(orderRepository, never())
                .save(any());
    }


    // =========================================================
    // HELPER METHOD
    // =========================================================

    private Order createOrder(
            Long id,
            String email,
            OrderStatus status,
            double totalAmount) {

        Order order = new Order();

        order.setId(id);
        order.setCustomerName("Praveen");
        order.setCustomerEmail(email);
        order.setStatus(status);
        order.setTotalAmount(totalAmount);
        order.setOrderItems(new ArrayList<>());
        order.setStatusHistory(new ArrayList<>());

        return order;
    }
}