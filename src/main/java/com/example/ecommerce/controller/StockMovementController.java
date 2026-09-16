package com.example.ecommerce.controller;

import com.example.ecommerce.dto.StockMovementResponse;
import com.example.ecommerce.service.StockMovementService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock-movements")
public class StockMovementController {

    private final StockMovementService stockMovementService;

    public StockMovementController(
            StockMovementService stockMovementService) {

        this.stockMovementService = stockMovementService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public List<StockMovementResponse> getAllMovements() {

        return stockMovementService.getAllMovements();
    }

    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public List<StockMovementResponse> getProductHistory(
            @PathVariable Long productId) {

        return stockMovementService.getProductHistory(productId);
    }
}