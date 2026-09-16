package com.example.ecommerce.controller;

import com.example.ecommerce.dto.PurchaseOrderRequest;
import com.example.ecommerce.entity.PurchaseOrder;
import com.example.ecommerce.service.PurchaseOrderService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    public PurchaseOrderController(
            PurchaseOrderService purchaseOrderService) {

        this.purchaseOrderService =
                purchaseOrderService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public PurchaseOrder createPurchaseOrder(
            @RequestBody PurchaseOrderRequest request) {

        return purchaseOrderService
                .createPurchaseOrder(request);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public List<PurchaseOrder> getAllPurchaseOrders() {

        return purchaseOrderService
                .getAllPurchaseOrders();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public PurchaseOrder getPurchaseOrder(
            @PathVariable Long id) {

        return purchaseOrderService
                .getPurchaseOrderById(id);
    }

    @PutMapping("/{id}/ordered")
    @PreAuthorize("hasRole('ADMIN')")
    public PurchaseOrder markAsOrdered(
            @PathVariable Long id) {

        return purchaseOrderService
                .markAsOrdered(id);
    }

    @PutMapping("/{id}/receive")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public PurchaseOrder receivePurchaseOrder(
            @PathVariable Long id) {

        return purchaseOrderService
                .receivePurchaseOrder(id);
    }
}
