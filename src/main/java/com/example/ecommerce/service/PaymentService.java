package com.example.ecommerce.service;

import com.example.ecommerce.dto.PaymentResponse;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.OrderStatus;
import com.example.ecommerce.entity.OrderStatusHistory;
import com.example.ecommerce.entity.Payment;
import com.example.ecommerce.entity.PaymentMethod;
import com.example.ecommerce.entity.PaymentStatus;
import com.example.ecommerce.exception.AccessDeniedException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public PaymentService(
            PaymentRepository paymentRepository,
            OrderRepository orderRepository) {

        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public PaymentResponse makePayment(
            Long orderId,
            PaymentMethod paymentMethod,
            String customerEmail) {

        // Find order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Order not found"));

        // Check whether the order belongs to the
        // logged-in customer
        if (!order.getCustomerEmail()
                .equals(customerEmail)) {

            throw new AccessDeniedException(
                    "You are not allowed to pay for this order");
        }

        // Cancelled order cannot be paid
        if (order.getStatus() != OrderStatus.PLACED) {

            throw new IllegalArgumentException(
                    "Payment is allowed only for placed orders"
            );
        }

        // Check duplicate payment
        if (paymentRepository
                .findByOrderId(orderId)
                .isPresent()) {

            throw new IllegalArgumentException(
                    "Payment already exists for this order");
        }

        // Create payment
        Payment payment = new Payment();

        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setPaymentMethod(paymentMethod);

        // Simulated successful payment
        payment.setStatus(PaymentStatus.SUCCESS);

        // Generate transaction ID
        payment.setTransactionId(
                UUID.randomUUID().toString()
        );

        payment.setPaymentDate(
                LocalDateTime.now()
        );

        // Save payment
        Payment savedPayment =
                paymentRepository.save(payment);

        // Successful payment:
        // PLACED → CONFIRMED
        if (order.getStatus() ==
                OrderStatus.PLACED) {

            order.setStatus(
                    OrderStatus.CONFIRMED
            );

            addStatusHistory(
                    order,
                    OrderStatus.CONFIRMED
            );

            orderRepository.save(order);
        }

        return convertToResponse(savedPayment);
    }

    public PaymentResponse getPaymentByOrderId(
            Long orderId) {

        Payment payment =
                paymentRepository.findByOrderId(orderId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Payment not found for order ID: "
                                                + orderId));

        return convertToResponse(payment);
    }

    private void addStatusHistory(
            Order order,
            OrderStatus status) {

        OrderStatusHistory history =
                new OrderStatusHistory();

        history.setOrder(order);
        history.setStatus(status);
        history.setChangedAt(
                LocalDateTime.now()
        );

        order.getStatusHistory().add(history);
    }

    private PaymentResponse convertToResponse(
            Payment payment) {

        PaymentResponse response =
                new PaymentResponse();

        response.setPaymentId(
                payment.getId()
        );

        response.setOrderId(
                payment.getOrder().getId()
        );

        response.setAmount(
                payment.getAmount()
        );

        response.setPaymentMethod(
                payment.getPaymentMethod()
        );

        response.setStatus(
                payment.getStatus()
        );

        response.setTransactionId(
                payment.getTransactionId()
        );

        response.setPaymentDate(
                payment.getPaymentDate()
        );

        return response;
    }
}