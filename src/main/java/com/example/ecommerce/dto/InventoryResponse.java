package com.example.ecommerce.dto;

public class InventoryResponse {

    private Long productId;
    private String productName;
    private Integer quantity;
    private Integer lowStockThreshold;
    private String stockStatus;

    public InventoryResponse(
            Long productId,
            String productName,
            Integer quantity,
            Integer lowStockThreshold,
            String stockStatus) {

        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.lowStockThreshold = lowStockThreshold;
        this.stockStatus = stockStatus;
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

    public Integer getLowStockThreshold() {
        return lowStockThreshold;
    }

    public String getStockStatus() {
        return stockStatus;
    }
}