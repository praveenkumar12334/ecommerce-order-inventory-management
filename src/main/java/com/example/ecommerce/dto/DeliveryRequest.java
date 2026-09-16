package com.example.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;

public class DeliveryRequest {

    private String deliveryAddress;

    private String deliveryPerson;

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getDeliveryPerson() {
        return deliveryPerson;
    }

    public void setDeliveryPerson(String deliveryPerson) {
        this.deliveryPerson = deliveryPerson;
    }
}