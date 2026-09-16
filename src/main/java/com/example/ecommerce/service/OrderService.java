package com.example.ecommerce.service;

import com.example.ecommerce.dto.OrderItemRequest;
import com.example.ecommerce.dto.OrderItemResponse;
import com.example.ecommerce.dto.OrderRequest;
import com.example.ecommerce.dto.OrderResponse;
import com.example.ecommerce.entity.*;
import com.example.ecommerce.exception.AccessDeniedException;
import com.example.ecommerce.exception.InsufficientStockException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.InventoryRepository;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final StockMovementService stockMovementService;
    private final AddressService addressService;

    public OrderService(
            OrderRepository orderRepository,
            ProductRepository productRepository,
            InventoryRepository inventoryRepository,
            StockMovementService stockMovementService,
            AddressService addressService) {

        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.stockMovementService = stockMovementService;
        this.addressService = addressService;
    }


    // =========================================================
    // CREATE ORDER
    // =========================================================

    @Transactional
    public OrderResponse saveOrder(
            OrderRequest request,
            String loggedInEmail) {

        Address address =
                addressService.getMyAddress(
                        request.getAddressId(),
                        loggedInEmail);

        Order order = new Order();

        // Use email from JWT
        order.setCustomerEmail(loggedInEmail);

        order.setDeliveryAddress(
                address.getAddressLine() + ", "
                        + address.getCity() + ", "
                        + address.getState() + " - "
                        + address.getPincode()
        );

        if (request.getItems() == null ||
                request.getItems().isEmpty()) {

            throw new IllegalArgumentException(
                    "Order must contain at least one item"
            );
        }



        order.setCustomerName(
                request.getCustomerName()
        );

        order.setOrderDate(
                LocalDateTime.now()
        );

        order.setStatus(
                OrderStatus.PLACED
        );

        order.setTotalAmount(0.0);

        if (order.getOrderItems() == null) {
            order.setOrderItems(new ArrayList<>());
        }

        double totalAmount = 0.0;


        // =====================================================
        // PROCESS ORDER ITEMS
        // =====================================================

        for (OrderItemRequest itemRequest :
                request.getItems()) {

            // Validate quantity
            if (itemRequest.getQuantity() <= 0) {

                throw new IllegalArgumentException(
                        "Quantity must be greater than 0"
                );
            }


            // -------------------------------------------------
            // Find Product
            // -------------------------------------------------

            Product product =
                    productRepository.findById(
                            itemRequest.getProductId()
                    ).orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Product not found with ID: "
                                            + itemRequest.getProductId()
                            )
                    );


            // -------------------------------------------------
            // Find Inventory
            // -------------------------------------------------

            Inventory inventory =
                    inventoryRepository
                            .findByProductId(product.getId())
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Inventory not found for product ID: "
                                                    + product.getId()
                                    )
                            );


            // -------------------------------------------------
            // Check Stock
            // -------------------------------------------------

            int availableStock =
                    inventory.getQuantity();

            int requestedQuantity =
                    itemRequest.getQuantity();

            if (requestedQuantity > availableStock) {

                throw new InsufficientStockException(
                        "Only "
                                + availableStock
                                + " units are available for product ID: "
                                + product.getId()
                );
            }


            // -------------------------------------------------
            // Create Order Item
            // -------------------------------------------------

            OrderItem orderItem =
                    new OrderItem();

            orderItem.setProduct(product);

            orderItem.setQuantity(
                    requestedQuantity
            );

            orderItem.setOrder(order);


            // -------------------------------------------------
            // Calculate Subtotal
            // -------------------------------------------------

            double price =
                    product.getPrice();

            double subtotal =
                    price * requestedQuantity;

            totalAmount =
                    totalAmount + subtotal;


            // -------------------------------------------------
            // Add item to order
            // -------------------------------------------------

            order.getOrderItems().add(
                    orderItem
            );


            // -------------------------------------------------
            // Reduce Inventory
            // -------------------------------------------------

            inventory.setQuantity(
                    availableStock - requestedQuantity
            );

            inventoryRepository.save(
                    inventory
            );
        }


        // =====================================================
        // SET TOTAL AMOUNT
        // =====================================================

        order.setTotalAmount(
                totalAmount
        );


        // =====================================================
        // ADD INITIAL STATUS HISTORY
        // =====================================================

        addStatusHistory(
                order,
                OrderStatus.PLACED
        );


        // =====================================================
        // SAVE ORDER FIRST
        // =====================================================

        Order savedOrder =
                orderRepository.save(order);


        // =====================================================
        // RECORD STOCK MOVEMENTS
        // =====================================================

        for (OrderItem item :
                savedOrder.getOrderItems()) {

            stockMovementService.recordMovement(
                    item.getProduct().getId(),
                    item.getQuantity(),
                    StockMovementType.ORDER_PLACED,
                    savedOrder.getId(),
                    "Stock reduced for customer order"
            );
        }


        // =====================================================
        // RETURN RESPONSE
        // =====================================================

        return convertToResponse(
                savedOrder
        );
    }


    // =========================================================
    // GET ALL ORDERS
    // =========================================================

    public List<OrderResponse> getAllOrders() {

        return orderRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }


    // =========================================================
    // GET ORDER BY ID
    // =========================================================

    public OrderResponse getOrderById(
            Long orderId) {

        Order order =
                orderRepository.findById(orderId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Order not found with ID: "
                                                + orderId
                                )
                        );

        return convertToResponse(order);
    }


    // =========================================================
    // GET MY ORDERS
    // =========================================================

    public List<OrderResponse> getMyOrders(
            String email) {

        return orderRepository
                .findByCustomerEmail(email)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }


    // =========================================================
    // GET MY ORDER BY ID
    // =========================================================

    public OrderResponse getMyOrderById(
            Long orderId,
            String email) {

        Order order =
                orderRepository.findById(orderId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Order not found with ID: "
                                                + orderId
                                )
                        );


        // Check ownership
        if (!order.getCustomerEmail()
                .equalsIgnoreCase(email)) {

            throw new AccessDeniedException(
                    "You are not allowed to access this order"
            );
        }


        return convertToResponse(order);
    }


    // =========================================================
    // CANCEL MY ORDER
    // =========================================================

    @Transactional
    public OrderResponse cancelMyOrder(
            Long orderId,
            String email) {

        Order order =
                orderRepository.findById(orderId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Order not found with ID: "
                                                + orderId
                                )
                        );


        // -------------------------------------------------
        // Check ownership
        // -------------------------------------------------

        if (!order.getCustomerEmail()
                .equalsIgnoreCase(email)) {

            throw new AccessDeniedException(
                    "You are not allowed to cancel this order"
            );
        }


        // -------------------------------------------------
        // Check current status
        // -------------------------------------------------

        OrderStatus currentStatus =
                order.getStatus();


        if (currentStatus ==
                OrderStatus.SHIPPED) {

            throw new IllegalStateException(
                    "Shipped orders cannot be cancelled"
            );
        }


        if (currentStatus ==
                OrderStatus.DELIVERED) {

            throw new IllegalStateException(
                    "Delivered orders cannot be cancelled"
            );
        }


        if (currentStatus ==
                OrderStatus.CANCELLED) {

            throw new IllegalStateException(
                    "Order is already cancelled"
            );
        }


        // =================================================
        // RESTORE STOCK
        // =================================================

        for (OrderItem item :
                order.getOrderItems()) {

            Long productId =
                    item.getProduct().getId();

            int quantity =
                    item.getQuantity();


            Inventory inventory =
                    inventoryRepository
                            .findByProductId(productId)
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Inventory not found for product ID: "
                                                    + productId
                                    )
                            );


            // Restore stock
            inventory.setQuantity(
                    inventory.getQuantity()
                            + quantity
            );

            inventoryRepository.save(
                    inventory
            );


            // Record stock movement
            stockMovementService.recordMovement(
                    productId,
                    quantity,
                    StockMovementType.ORDER_CANCELLED,
                    order.getId(),
                    "Stock restored after order cancellation"
            );
        }


        // =================================================
        // CHANGE STATUS
        // =================================================

        order.setStatus(
                OrderStatus.CANCELLED
        );


        // =================================================
        // ADD STATUS HISTORY
        // =================================================

        addStatusHistory(
                order,
                OrderStatus.CANCELLED
        );


        // =================================================
        // SAVE ORDER
        // =================================================

        Order savedOrder =
                orderRepository.save(order);


        return convertToResponse(
                savedOrder
        );
    }


    // =========================================================
    // UPDATE ORDER STATUS
    // =========================================================

    @Transactional
    public OrderResponse updateOrderStatus(
            Long orderId,
            OrderStatus newStatus) {

        Order order =
                orderRepository.findById(orderId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Order not found with ID: "
                                                + orderId
                                )
                        );


        OrderStatus currentStatus =
                order.getStatus();


        // Validate transition
        validateStatusTransition(
                currentStatus,
                newStatus
        );


        // Change status
        order.setStatus(
                newStatus
        );


        // Add history
        addStatusHistory(
                order,
                newStatus
        );


        // Save
        Order savedOrder =
                orderRepository.save(order);


        return convertToResponse(
                savedOrder
        );
    }


    // =========================================================
    // VALIDATE STATUS TRANSITION
    // =========================================================

    private void validateStatusTransition(
            OrderStatus currentStatus,
            OrderStatus newStatus) {

        if (currentStatus ==
                OrderStatus.CANCELLED) {

            throw new IllegalStateException(
                    "Cancelled orders cannot change status"
            );
        }


        if (currentStatus ==
                OrderStatus.DELIVERED) {

            throw new IllegalStateException(
                    "Delivered orders cannot change status"
            );
        }


        // PLACED → CONFIRMED
        if (currentStatus ==
                OrderStatus.PLACED
                &&
                newStatus ==
                        OrderStatus.CONFIRMED) {

            return;
        }


        // CONFIRMED → SHIPPED
        if (currentStatus ==
                OrderStatus.CONFIRMED
                &&
                newStatus ==
                        OrderStatus.SHIPPED) {

            return;
        }


        // SHIPPED → DELIVERED
        if (currentStatus ==
                OrderStatus.SHIPPED
                &&
                newStatus ==
                        OrderStatus.DELIVERED) {

            return;
        }


        // Same status
        if (currentStatus == newStatus) {

            throw new IllegalStateException(
                    "Order is already in "
                            + newStatus
                            + " status"
            );
        }


        // Invalid transition
        throw new IllegalStateException(
                "Invalid order status transition from "
                        + currentStatus
                        + " to "
                        + newStatus
        );
    }


    // =========================================================
    // ADD STATUS HISTORY
    // =========================================================

    private void addStatusHistory(
            Order order,
            OrderStatus status) {

        OrderStatusHistory history =
                new OrderStatusHistory();

        history.setStatus(
                status
        );

        history.setChangedAt(
                LocalDateTime.now()
        );

        history.setOrder(
                order
        );


        if (order.getStatusHistory() == null) {

            order.setStatusHistory(
                    new ArrayList<>()
            );
        }


        order.getStatusHistory().add(
                history
        );
    }


    // =========================================================
    // CONVERT ORDER TO RESPONSE
    // =========================================================

    private OrderResponse convertToResponse(
            Order order) {

        List<OrderItemResponse> itemResponses =
                new ArrayList<>();


        if (order.getOrderItems() != null) {

            for (OrderItem item :
                    order.getOrderItems()) {

                Product product =
                        item.getProduct();


                double price =
                        product.getPrice();


                double subtotal =
                        price * item.getQuantity();


                OrderItemResponse itemResponse =
                        new OrderItemResponse(
                                product.getId(),
                                product.getName(),
                                item.getQuantity(),
                                price,
                                subtotal
                        );


                itemResponses.add(
                        itemResponse
                );
            }
        }


        return new OrderResponse(
                order.getId(),
                order.getCustomerName(),
                order.getCustomerEmail(),
                order.getOrderDate(),
                order.getStatus(),
                order.getTotalAmount(),
                itemResponses
        );
    }
}