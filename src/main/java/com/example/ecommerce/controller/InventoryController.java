package com.example.ecommerce.controller;

import com.example.ecommerce.entity.Inventory;
import com.example.ecommerce.service.InventoryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    // ----------------------------------------------------
    // GET ALL INVENTORY
    // Admin and Staff
    // ----------------------------------------------------

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public List<Inventory> getAllInventory() {

        return inventoryService.getAllInventory();
    }


    // ----------------------------------------------------
    // GET INVENTORY FOR A PRODUCT
    // Admin and Staff
    // ----------------------------------------------------

    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public Inventory getInventoryByProductId(
            @PathVariable Long productId) {

        return inventoryService.getInventoryByProductId(productId);
    }


    // ----------------------------------------------------
    // GET LOW STOCK PRODUCTS
    // Admin and Staff
    // ----------------------------------------------------

    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public List<Inventory> getLowStockProducts() {

        return inventoryService.getLowStockProducts();
    }


    // ----------------------------------------------------
    // UPDATE STOCK DIRECTLY
    // Admin only
    // ----------------------------------------------------

    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Inventory updateStock(
            @PathVariable Long productId,
            @RequestParam int quantity) {

        return inventoryService.updateStock(
                productId,
                quantity
        );
    }


    // ----------------------------------------------------
    // MANUAL STOCK ADJUSTMENT
    // Admin only
    // ----------------------------------------------------

    @PutMapping("/{productId}/adjust")
    @PreAuthorize("hasRole('ADMIN')")
    public Inventory adjustStock(
            @PathVariable Long productId,
            @RequestParam int quantity,
            @RequestParam(required = false) String reason) {

        return inventoryService.adjustStock(
                productId,
                quantity,
                reason
        );
    }
}