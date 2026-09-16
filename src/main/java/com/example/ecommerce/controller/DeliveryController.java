package com.example.ecommerce.controller;

import com.example.ecommerce.dto.DeliveryRequest;
import com.example.ecommerce.dto.DeliveryResponse;
import com.example.ecommerce.entity.DeliveryStatus;
import com.example.ecommerce.service.DeliveryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    // ----------------------------------------------------
    // CREATE DELIVERY
    // ADMIN / STAFF
    // ----------------------------------------------------

    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @PostMapping("/order/{orderId}")
    public DeliveryResponse createDelivery(
            @PathVariable Long orderId,
            @RequestBody DeliveryRequest request) {

        return deliveryService.createDelivery(
                orderId,
                request
        );
    }


    // ----------------------------------------------------
    // ASSIGN DELIVERY PERSON
    // ADMIN / STAFF
    // ----------------------------------------------------

    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @PutMapping("/{deliveryId}/assign")
    public DeliveryResponse assignDelivery(
            @PathVariable Long deliveryId,
            @RequestParam String deliveryPerson) {

        return deliveryService.assignDelivery(
                deliveryId,
                deliveryPerson
        );
    }


    // ----------------------------------------------------
    // UPDATE DELIVERY STATUS
    // ADMIN / STAFF
    // ----------------------------------------------------

    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @PutMapping("/{deliveryId}/status")
    public DeliveryResponse updateStatus(
            @PathVariable Long deliveryId,
            @RequestParam DeliveryStatus status) {

        return deliveryService.updateStatus(
                deliveryId,
                status
        );
    }


    // ----------------------------------------------------
    // GET DELIVERY BY ORDER
    // ADMIN / STAFF
    // ----------------------------------------------------

    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @GetMapping("/order/{orderId}")
    public DeliveryResponse getDelivery(
            @PathVariable Long orderId) {

        return deliveryService.getDeliveryByOrderId(
                orderId
        );
    }
}