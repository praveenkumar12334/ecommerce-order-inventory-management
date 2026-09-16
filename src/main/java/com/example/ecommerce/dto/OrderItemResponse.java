package com.example.ecommerce.dto;

public class OrderItemResponse {

    private Long productId;
    private String productName;
    private int quantity;
    private double price;
    private double subtotal;

    public OrderItemResponse(Long productId,
                             String productName,
                             int quantity,
                             double price,
                             double subtotal) {

        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
        this.subtotal = subtotal;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public double getSubtotal() {
        return subtotal;
    }
}