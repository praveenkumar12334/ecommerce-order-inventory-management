package com.example.ecommerce.dto;

import com.example.ecommerce.entity.StockMovementType;

import java.time.LocalDateTime;

public class StockMovementResponse {

    private Long id;
    private Long productId;
    private String productName;
    private Integer quantity;
    private StockMovementType type;
    private Long referenceId;
    private String note;
    private LocalDateTime createdAt;

    public StockMovementResponse(Long id,
                                 Long productId,
                                 String productName,
                                 Integer quantity,
                                 StockMovementType type,
                                 Long referenceId,
                                 String note,
                                 LocalDateTime createdAt) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.type = type;
        this.referenceId = referenceId;
        this.note = note;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public StockMovementType getType() {
        return type;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public String getNote() {
        return note;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}