package com.example.ecommerce.service;

import com.example.ecommerce.dto.StockMovementResponse;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.StockMovement;
import com.example.ecommerce.entity.StockMovementType;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.StockMovementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockMovementServiceTest {

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private StockMovementService stockMovementService;

    private Product product;
    private StockMovement movement;

    @BeforeEach
    void setUp() {

        product = new Product();
        product.setId(1L);
        product.setName("Laptop");
        product.setPrice(50000.0);

        movement = new StockMovement();
        movement.setProduct(product);
        movement.setQuantity(10);
        movement.setType(StockMovementType.PURCHASE_RECEIVED);
        movement.setReferenceId(100L);
        movement.setNote("Stock received from purchase order");
    }

    // ---------------------------------------------------------
    // recordMovement()
    // ---------------------------------------------------------

    @Test
    void recordMovement_success() {

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        stockMovementService.recordMovement(
                1L,
                10,
                StockMovementType.PURCHASE_RECEIVED,
                100L,
                "Stock received"
        );

        verify(productRepository).findById(1L);
        verify(stockMovementRepository).save(any(StockMovement.class));
    }

    @Test
    void recordMovement_productNotFound() {

        when(productRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> stockMovementService.recordMovement(
                        1L,
                        10,
                        StockMovementType.PURCHASE_RECEIVED,
                        100L,
                        "Stock received"
                )
        );

        verify(stockMovementRepository, never())
                .save(any(StockMovement.class));
    }

    @Test
    void recordMovement_correctDataSaved() {

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        stockMovementService.recordMovement(
                1L,
                15,
                StockMovementType.ORDER_PLACED,
                200L,
                "Order placed"
        );

        verify(stockMovementRepository).save(
                argThat(saved ->

                        saved.getProduct() == product
                                && saved.getQuantity() == 15
                                && saved.getType()
                                == StockMovementType.ORDER_PLACED
                                && saved.getReferenceId() == 200L
                                && saved.getNote()
                                .equals("Order placed")
                )
        );
    }

    // ---------------------------------------------------------
    // getProductHistory()
    // ---------------------------------------------------------

    @Test
    void getProductHistory_success() {

        when(stockMovementRepository
                .findByProductIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(movement));

        List<StockMovementResponse> result =
                stockMovementService.getProductHistory(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getProductHistory_empty() {

        when(stockMovementRepository
                .findByProductIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of());

        List<StockMovementResponse> result =
                stockMovementService.getProductHistory(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getProductHistory_responseDataCorrect() {

        when(stockMovementRepository
                .findByProductIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(movement));

        List<StockMovementResponse> result =
                stockMovementService.getProductHistory(1L);

        StockMovementResponse response = result.getFirst();

        assertEquals(1L, response.getProductId());
        assertEquals("Laptop", response.getProductName());
        assertEquals(10, response.getQuantity());
        assertEquals(
                StockMovementType.PURCHASE_RECEIVED,
                response.getType()
        );
        assertEquals(100L, response.getReferenceId());
        assertEquals(
                "Stock received from purchase order",
                response.getNote()
        );
    }

    @Test
    void getProductHistory_repositoryCalledCorrectly() {

        when(stockMovementRepository
                .findByProductIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of());

        stockMovementService.getProductHistory(1L);

        verify(stockMovementRepository)
                .findByProductIdOrderByCreatedAtDesc(1L);
    }

    // ---------------------------------------------------------
    // getAllMovements()
    // ---------------------------------------------------------

    @Test
    void getAllMovements_success() {

        StockMovement movement2 = new StockMovement();
        movement2.setProduct(product);
        movement2.setQuantity(5);
        movement2.setType(StockMovementType.ORDER_PLACED);
        movement2.setReferenceId(200L);
        movement2.setNote("Order placed");

        when(stockMovementRepository
                .findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(movement, movement2));

        List<StockMovementResponse> result =
                stockMovementService.getAllMovements();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void getAllMovements_empty() {

        when(stockMovementRepository
                .findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of());

        List<StockMovementResponse> result =
                stockMovementService.getAllMovements();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllMovements_responseDataCorrect() {

        when(stockMovementRepository
                .findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(movement));

        List<StockMovementResponse> result =
                stockMovementService.getAllMovements();

        StockMovementResponse response = result.getFirst();

        assertEquals(1L, response.getProductId());
        assertEquals("Laptop", response.getProductName());
        assertEquals(10, response.getQuantity());
        assertEquals(
                StockMovementType.PURCHASE_RECEIVED,
                response.getType()
        );
        assertEquals(100L, response.getReferenceId());
        assertEquals(
                "Stock received from purchase order",
                response.getNote()
        );
    }

    @Test
    void getAllMovements_repositoryCalledCorrectly() {

        when(stockMovementRepository
                .findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of());

        stockMovementService.getAllMovements();

        verify(stockMovementRepository)
                .findAllByOrderByCreatedAtDesc();
    }

    // ---------------------------------------------------------
    // Conversion / multiple records
    // ---------------------------------------------------------

    @Test
    void getProductHistory_multipleMovements() {

        StockMovement movement2 = new StockMovement();
        movement2.setProduct(product);
        movement2.setQuantity(3);
        movement2.setType(StockMovementType.MANUAL_ADJUSTMENT);
        movement2.setReferenceId(null);
        movement2.setNote("Manual stock adjustment");

        when(stockMovementRepository
                .findByProductIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(movement, movement2));

        List<StockMovementResponse> result =
                stockMovementService.getProductHistory(1L);

        assertEquals(2, result.size());

        assertEquals(10, result.get(0).getQuantity());
        assertEquals(3, result.get(1).getQuantity());
    }

    @Test
    void getAllMovements_multipleProducts() {

        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Phone");
        product2.setPrice(20000.0);

        StockMovement movement2 = new StockMovement();
        movement2.setProduct(product2);
        movement2.setQuantity(7);
        movement2.setType(StockMovementType.PURCHASE_RECEIVED);
        movement2.setReferenceId(300L);
        movement2.setNote("Phone stock received");

        when(stockMovementRepository
                .findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(movement, movement2));

        List<StockMovementResponse> result =
                stockMovementService.getAllMovements();

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getProductId());
        assertEquals(2L, result.get(1).getProductId());
    }
}