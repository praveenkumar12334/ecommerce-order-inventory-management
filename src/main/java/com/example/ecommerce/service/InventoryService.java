package com.example.ecommerce.service;

import com.example.ecommerce.entity.Inventory;
import com.example.ecommerce.exception.InsufficientStockException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final StockMovementService stockMovementService;

    public InventoryService(
            InventoryRepository inventoryRepository,
            StockMovementService stockMovementService) {

        this.inventoryRepository = inventoryRepository;
        this.stockMovementService = stockMovementService;
    }

    // ----------------------------------------------------
    // CREATE / SAVE INVENTORY
    // ----------------------------------------------------

    @Transactional
    public Inventory saveInventory(Inventory inventory) {

        if (inventory.getQuantity()  < 0) {
            throw new IllegalArgumentException(
                    "Stock quantity cannot be negative"
            );
        }

        if (inventory.getLowStockThreshold() == null
                || inventory.getLowStockThreshold() < 0) {

            inventory.setLowStockThreshold(5);
        }

        return inventoryRepository.save(inventory);
    }


    // ----------------------------------------------------
    // GET INVENTORY BY PRODUCT
    // ----------------------------------------------------

    public Inventory getInventoryByProductId(Long productId) {

        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Inventory not found for product ID: "
                                        + productId
                        )
                );
    }


    // ----------------------------------------------------
    // GET ALL INVENTORY
    // ----------------------------------------------------

    public List<Inventory> getAllInventory() {

        return inventoryRepository.findAll();
    }


    // ----------------------------------------------------
    // REDUCE STOCK
    // Used when customer places an order
    // ----------------------------------------------------

    @Transactional
    public void reduceStock(Long productId, int quantity) {

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }

        Inventory inventory = inventoryRepository
                .findByProductId(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Inventory not found for product ID: "
                                        + productId
                        )
                );

        if (inventory.getQuantity() < quantity) {

            throw new InsufficientStockException(
                    "Insufficient stock. Available stock: "
                            + inventory.getQuantity()
            );
        }

        int remainingStock =
                inventory.getQuantity() - quantity;

        inventory.setQuantity(remainingStock);

        inventoryRepository.save(inventory);
    }


    // ----------------------------------------------------
    // RESTORE STOCK
    // Used when customer cancels an order
    // ----------------------------------------------------

    @Transactional
    public void restoreStock(Long productId, int quantity) {

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }

        Inventory inventory = inventoryRepository
                .findByProductId(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Inventory not found for product ID: "
                                        + productId
                        )
                );

        inventory.setQuantity(
                inventory.getQuantity() + quantity
        );

        inventoryRepository.save(inventory);
    }


    // ----------------------------------------------------
    // UPDATE STOCK DIRECTLY
    // Admin operation
    // ----------------------------------------------------

    @Transactional
    public Inventory updateStock(
            Long productId,
            int quantity) {

        if (quantity < 0) {
            throw new IllegalArgumentException(
                    "Stock quantity cannot be negative"
            );
        }

        Inventory inventory = inventoryRepository
                .findByProductId(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Inventory not found for product ID: "
                                        + productId
                        )
                );

        inventory.setQuantity(quantity);

        return inventoryRepository.save(inventory);
    }


    // ----------------------------------------------------
    // LOW STOCK PRODUCTS
    // ----------------------------------------------------

    public List<Inventory> getLowStockProducts() {

        return inventoryRepository.findAll()
                .stream()
                .filter(inventory ->
                        inventory.getQuantity()
                                <= inventory.getLowStockThreshold()
                )
                .toList();
    }


    // ----------------------------------------------------
    // MANUAL STOCK ADJUSTMENT
    // Admin can add or remove stock
    //
    // Example:
    // +20 -> add 20 stock
    // -5  -> remove 5 stock
    // ----------------------------------------------------

    @Transactional
    public Inventory adjustStock(
            Long productId,
            int quantity,
            String reason) {

        if (quantity == 0) {
            throw new IllegalArgumentException(
                    "Adjustment quantity cannot be zero"
            );
        }

        Inventory inventory = inventoryRepository
                .findByProductId(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Inventory not found for product ID: "
                                        + productId
                        )
                );

        int newQuantity =
                inventory.getQuantity() + quantity;

        if (newQuantity < 0) {
            throw new InsufficientStockException(
                    "Stock cannot become negative. Available stock: "
                            + inventory.getQuantity()
            );
        }

        inventory.setQuantity(newQuantity);

        Inventory savedInventory =
                inventoryRepository.save(inventory);


        // Record stock movement
        stockMovementService.recordMovement(
                productId,
                Math.abs(quantity),
                com.example.ecommerce.entity.StockMovementType.MANUAL_ADJUSTMENT,
                null,
                reason
        );

        return savedInventory;
    }
}