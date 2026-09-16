package com.example.ecommerce.service;

import com.example.ecommerce.entity.Inventory;
import com.example.ecommerce.entity.StockMovementType;
import com.example.ecommerce.exception.InsufficientStockException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private StockMovementService stockMovementService;

    @InjectMocks
    private InventoryService inventoryService;

    private Inventory inventory;

    @BeforeEach
    void setUp() {

        inventory = new Inventory();

        inventory.setId(1L);
        inventory.setQuantity(20);
        inventory.setLowStockThreshold(5);
    }

    // =========================================================
    // saveInventory()
    // =========================================================

    @Test
    void saveInventory_shouldSaveInventory() {

        when(inventoryRepository.save(inventory))
                .thenReturn(inventory);

        Inventory result =
                inventoryService.saveInventory(inventory);

        assertNotNull(result);
        assertEquals(20, result.getQuantity());

        verify(inventoryRepository).save(inventory);
    }

    @Test
    void saveInventory_shouldRejectNegativeQuantity() {

        inventory.setQuantity(-1);

        assertThrows(
                IllegalArgumentException.class,
                () -> inventoryService.saveInventory(inventory)
        );

        verify(inventoryRepository, never())
                .save(any(Inventory.class));
    }

    @Test
    void saveInventory_shouldSetDefaultThresholdWhenNull() {

        inventory.setLowStockThreshold(null);

        when(inventoryRepository.save(inventory))
                .thenReturn(inventory);

        Inventory result =
                inventoryService.saveInventory(inventory);

        assertEquals(
                5,
                result.getLowStockThreshold()
        );

        verify(inventoryRepository).save(inventory);
    }

    @Test
    void saveInventory_shouldSetDefaultThresholdWhenNegative() {

        inventory.setLowStockThreshold(-1);

        when(inventoryRepository.save(inventory))
                .thenReturn(inventory);

        Inventory result =
                inventoryService.saveInventory(inventory);

        assertEquals(
                5,
                result.getLowStockThreshold()
        );

        verify(inventoryRepository).save(inventory);
    }

    @Test
    void saveInventory_shouldKeepValidThreshold() {

        inventory.setLowStockThreshold(10);

        when(inventoryRepository.save(inventory))
                .thenReturn(inventory);

        Inventory result =
                inventoryService.saveInventory(inventory);

        assertEquals(
                10,
                result.getLowStockThreshold()
        );
    }

    // =========================================================
    // getInventoryByProductId()
    // =========================================================

    @Test
    void getInventoryByProductId_shouldReturnInventory() {

        when(inventoryRepository.findByProductId(100L))
                .thenReturn(Optional.of(inventory));

        Inventory result =
                inventoryService.getInventoryByProductId(100L);

        assertNotNull(result);
        assertEquals(20, result.getQuantity());
    }

    @Test
    void getInventoryByProductId_shouldThrowWhenNotFound() {

        when(inventoryRepository.findByProductId(100L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> inventoryService
                        .getInventoryByProductId(100L)
        );
    }

    // =========================================================
    // getAllInventory()
    // =========================================================

    @Test
    void getAllInventory_shouldReturnAllInventory() {

        Inventory secondInventory = new Inventory();
        secondInventory.setId(2L);
        secondInventory.setQuantity(10);
        secondInventory.setLowStockThreshold(5);

        when(inventoryRepository.findAll())
                .thenReturn(List.of(
                        inventory,
                        secondInventory
                ));

        List<Inventory> result =
                inventoryService.getAllInventory();

        assertEquals(2, result.size());
        assertEquals(20, result.getFirst().getQuantity());
        assertEquals(10, result.get(1).getQuantity());
    }

    @Test
    void getAllInventory_shouldReturnEmptyList() {

        when(inventoryRepository.findAll())
                .thenReturn(List.of());

        List<Inventory> result =
                inventoryService.getAllInventory();

        assertTrue(result.isEmpty());
    }

    // =========================================================
    // reduceStock()
    // =========================================================

    @Test
    void reduceStock_shouldReduceQuantity() {

        when(inventoryRepository.findByProductId(100L))
                .thenReturn(Optional.of(inventory));

        when(inventoryRepository.save(inventory))
                .thenReturn(inventory);

        inventoryService.reduceStock(100L, 5);

        assertEquals(15, inventory.getQuantity());

        verify(inventoryRepository).save(inventory);
    }

    @Test
    void reduceStock_shouldAllowExactAvailableQuantity() {

        when(inventoryRepository.findByProductId(100L))
                .thenReturn(Optional.of(inventory));

        inventoryService.reduceStock(100L, 20);

        assertEquals(0, inventory.getQuantity());

        verify(inventoryRepository).save(inventory);
    }

    @Test
    void reduceStock_shouldRejectZeroQuantity() {

        assertThrows(
                IllegalArgumentException.class,
                () -> inventoryService.reduceStock(100L, 0)
        );

        verify(inventoryRepository, never())
                .save(any(Inventory.class));
    }

    @Test
    void reduceStock_shouldRejectNegativeQuantity() {

        assertThrows(
                IllegalArgumentException.class,
                () -> inventoryService.reduceStock(100L, -5)
        );

        verify(inventoryRepository, never())
                .save(any(Inventory.class));
    }

    @Test
    void reduceStock_shouldThrowWhenInventoryNotFound() {

        when(inventoryRepository.findByProductId(100L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> inventoryService.reduceStock(100L, 5)
        );
    }

    @Test
    void reduceStock_shouldRejectInsufficientStock() {

        when(inventoryRepository.findByProductId(100L))
                .thenReturn(Optional.of(inventory));

        assertThrows(
                InsufficientStockException.class,
                () -> inventoryService.reduceStock(100L, 25)
        );

        assertEquals(20, inventory.getQuantity());

        verify(inventoryRepository, never())
                .save(any(Inventory.class));
    }

    // =========================================================
    // restoreStock()
    // =========================================================

    @Test
    void restoreStock_shouldIncreaseQuantity() {

        when(inventoryRepository.findByProductId(100L))
                .thenReturn(Optional.of(inventory));

        inventoryService.restoreStock(100L, 5);

        assertEquals(25, inventory.getQuantity());

        verify(inventoryRepository).save(inventory);
    }

    @Test
    void restoreStock_shouldRejectZeroQuantity() {

        assertThrows(
                IllegalArgumentException.class,
                () -> inventoryService.restoreStock(100L, 0)
        );

        verify(inventoryRepository, never())
                .save(any(Inventory.class));
    }

    @Test
    void restoreStock_shouldRejectNegativeQuantity() {

        assertThrows(
                IllegalArgumentException.class,
                () -> inventoryService.restoreStock(100L, -5)
        );

        verify(inventoryRepository, never())
                .save(any(Inventory.class));
    }

    @Test
    void restoreStock_shouldThrowWhenInventoryNotFound() {

        when(inventoryRepository.findByProductId(100L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> inventoryService.restoreStock(100L, 5)
        );
    }

    // =========================================================
    // updateStock()
    // =========================================================

    @Test
    void updateStock_shouldSetNewQuantity() {

        when(inventoryRepository.findByProductId(100L))
                .thenReturn(Optional.of(inventory));

        when(inventoryRepository.save(inventory))
                .thenReturn(inventory);

        Inventory result =
                inventoryService.updateStock(100L, 50);

        assertEquals(50, result.getQuantity());

        verify(inventoryRepository).save(inventory);
    }

    @Test
    void updateStock_shouldAllowZeroQuantity() {

        when(inventoryRepository.findByProductId(100L))
                .thenReturn(Optional.of(inventory));

        inventoryService.updateStock(100L, 0);

        assertEquals(0, inventory.getQuantity());
    }

    @Test
    void updateStock_shouldRejectNegativeQuantity() {

        assertThrows(
                IllegalArgumentException.class,
                () -> inventoryService.updateStock(100L, -1)
        );

        verify(inventoryRepository, never())
                .save(any(Inventory.class));
    }

    @Test
    void updateStock_shouldThrowWhenInventoryNotFound() {

        when(inventoryRepository.findByProductId(100L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> inventoryService.updateStock(100L, 50)
        );
    }

    // =========================================================
    // getLowStockProducts()
    // =========================================================

    @Test
    void getLowStockProducts_shouldReturnLowStockItems() {

        Inventory lowStock = new Inventory();
        lowStock.setId(2L);
        lowStock.setQuantity(3);
        lowStock.setLowStockThreshold(5);

        Inventory normalStock = new Inventory();
        normalStock.setId(3L);
        normalStock.setQuantity(20);
        normalStock.setLowStockThreshold(5);

        when(inventoryRepository.findAll())
                .thenReturn(List.of(
                        lowStock,
                        normalStock
                ));

        List<Inventory> result =
                inventoryService.getLowStockProducts();

        assertEquals(1, result.size());
        assertEquals(
                3,
                result.getFirst().getQuantity()
        );
    }

    @Test
    void getLowStockProducts_shouldIncludeExactThreshold() {

        Inventory lowStock = new Inventory();
        lowStock.setQuantity(5);
        lowStock.setLowStockThreshold(5);

        when(inventoryRepository.findAll())
                .thenReturn(List.of(lowStock));

        List<Inventory> result =
                inventoryService.getLowStockProducts();

        assertEquals(1, result.size());
    }

    @Test
    void getLowStockProducts_shouldReturnEmptyWhenNoneAreLow() {

        Inventory normalStock = new Inventory();
        normalStock.setQuantity(20);
        normalStock.setLowStockThreshold(5);

        when(inventoryRepository.findAll())
                .thenReturn(List.of(normalStock));

        List<Inventory> result =
                inventoryService.getLowStockProducts();

        assertTrue(result.isEmpty());
    }

    // =========================================================
    // adjustStock()
    // =========================================================

    @Test
    void adjustStock_shouldIncreaseStock() {

        when(inventoryRepository.findByProductId(100L))
                .thenReturn(Optional.of(inventory));

        when(inventoryRepository.save(inventory))
                .thenReturn(inventory);

        Inventory result =
                inventoryService.adjustStock(
                        100L,
                        10,
                        "New stock received"
                );

        assertEquals(30, result.getQuantity());

        verify(stockMovementService).recordMovement(
                eq(100L),
                eq(10),
                eq(StockMovementType.MANUAL_ADJUSTMENT),
                isNull(),
                eq("New stock received")
        );
    }

    @Test
    void adjustStock_shouldDecreaseStock() {

        when(inventoryRepository.findByProductId(100L))
                .thenReturn(Optional.of(inventory));

        when(inventoryRepository.save(inventory))
                .thenReturn(inventory);

        Inventory result =
                inventoryService.adjustStock(
                        100L,
                        -5,
                        "Damaged products"
                );

        assertEquals(15, result.getQuantity());

        verify(stockMovementService).recordMovement(
                eq(100L),
                eq(5),
                eq(StockMovementType.MANUAL_ADJUSTMENT),
                isNull(),
                eq("Damaged products")
        );
    }

    @Test
    void adjustStock_shouldRejectZeroAdjustment() {

        assertThrows(
                IllegalArgumentException.class,
                () -> inventoryService.adjustStock(
                        100L,
                        0,
                        "Invalid adjustment"
                )
        );

        verify(inventoryRepository, never())
                .save(any(Inventory.class));

        verifyNoInteractions(stockMovementService);
    }

    @Test
    void adjustStock_shouldRejectNegativeResult() {

        when(inventoryRepository.findByProductId(100L))
                .thenReturn(Optional.of(inventory));

        assertThrows(
                InsufficientStockException.class,
                () -> inventoryService.adjustStock(
                        100L,
                        -25,
                        "Remove stock"
                )
        );

        assertEquals(20, inventory.getQuantity());

        verify(inventoryRepository, never())
                .save(any(Inventory.class));

        verifyNoInteractions(stockMovementService);
    }

    @Test
    void adjustStock_shouldAllowRemovingAllStock() {

        when(inventoryRepository.findByProductId(100L))
                .thenReturn(Optional.of(inventory));

        inventoryService.adjustStock(
                100L,
                -20,
                "Remove all stock"
        );

        assertEquals(0, inventory.getQuantity());

        verify(stockMovementService).recordMovement(
                eq(100L),
                eq(20),
                eq(StockMovementType.MANUAL_ADJUSTMENT),
                isNull(),
                eq("Remove all stock")
        );
    }

    @Test
    void adjustStock_shouldThrowWhenInventoryNotFound() {

        when(inventoryRepository.findByProductId(100L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> inventoryService.adjustStock(
                        100L,
                        10,
                        "Add stock"
                )
        );

        verify(inventoryRepository, never())
                .save(any(Inventory.class));

        verifyNoInteractions(stockMovementService);
    }
}