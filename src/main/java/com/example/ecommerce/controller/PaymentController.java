package com.example.ecommerce.controller;

import com.example.ecommerce.dto.PaymentRequest;
import com.example.ecommerce.dto.PaymentResponse;
import com.example.ecommerce.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(
            PaymentService paymentService) {

        this.paymentService = paymentService;
    }

    @PostMapping("/orders/{orderId}")
    public PaymentResponse makePayment(
            @PathVariable Long orderId,
            @RequestBody @Valid PaymentRequest request,
            @AuthenticationPrincipal
            UserDetails userDetails) {

        return paymentService.makePayment(
                orderId,
                request.getPaymentMethod(),
                userDetails.getUsername()
        );
    }
}