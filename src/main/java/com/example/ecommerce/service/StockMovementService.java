package com.example.ecommerce.service;

import com.example.ecommerce.dto.StockMovementResponse;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.StockMovement;
import com.example.ecommerce.entity.StockMovementType;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.StockMovementRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockMovementService {

    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;

    public StockMovementService(
            StockMovementRepository stockMovementRepository,
            ProductRepository productRepository) {

        this.stockMovementRepository = stockMovementRepository;
        this.productRepository = productRepository;
    }

    public void recordMovement(
            Long productId,
            Integer quantity,
            StockMovementType type,
            Long referenceId,
            String note) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found with ID: " + productId));

        StockMovement movement = new StockMovement();

        movement.setProduct(product);
        movement.setQuantity(quantity);
        movement.setType(type);
        movement.setReferenceId(referenceId);
        movement.setNote(note);

        stockMovementRepository.save(movement);
    }

    public List<StockMovementResponse> getProductHistory(Long productId) {

        List<StockMovement> movements =
                stockMovementRepository
                        .findByProductIdOrderByCreatedAtDesc(productId);

        return movements.stream()
                .map(this::convertToResponse)
                .toList();
    }

    public List<StockMovementResponse> getAllMovements() {

        return stockMovementRepository
                .findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    private StockMovementResponse convertToResponse(
            StockMovement movement) {

        return new StockMovementResponse(
                movement.getId(),
                movement.getProduct().getId(),
                movement.getProduct().getName(),
                movement.getQuantity(),
                movement.getType(),
                movement.getReferenceId(),
                movement.getNote(),
                movement.getCreatedAt()
        );
    }
}