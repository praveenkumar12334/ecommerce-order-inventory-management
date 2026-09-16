package com.example.ecommerce.controller;

import com.example.ecommerce.dto.OrderRequest;
import com.example.ecommerce.dto.OrderResponse;
import com.example.ecommerce.entity.OrderStatus;
import com.example.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public OrderResponse createOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid OrderRequest request) {

        return orderService.saveOrder(
                request,
                userDetails.getUsername()
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @GetMapping
    public List<OrderResponse> getAllOrders() {
        return orderService.getAllOrders();
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @GetMapping("/{orderId}")
    public OrderResponse getOrderById(@PathVariable Long orderId) {
        return orderService.getOrderById(orderId);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @PutMapping("/{orderId}/status")
    public OrderResponse updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status) {

        return orderService.updateOrderStatus(
                orderId,
                status
        );
    }

    @PutMapping("/my-orders/{orderId}/cancel")
    public OrderResponse cancelMyOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId) {

        return orderService.cancelMyOrder(
                orderId,
                userDetails.getUsername()
        );
    }

    @GetMapping("/my-orders")
    public List<OrderResponse> getMyOrders(
            @AuthenticationPrincipal UserDetails userDetails) {

        return orderService.getMyOrders(
                userDetails.getUsername()
        );
    }

    @GetMapping("/my-orders/{orderId}")
    public OrderResponse getMyOrderById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId) {

        return orderService.getMyOrderById(
                orderId,
                userDetails.getUsername()
        );
    }
}