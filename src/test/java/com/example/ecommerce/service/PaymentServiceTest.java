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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private PaymentService paymentService;

    private Order order;

    @BeforeEach
    void setUp() {

        order = new Order();

        order.setId(1L);
        order.setCustomerEmail("user@gmail.com");
        order.setStatus(OrderStatus.PLACED);
        order.setTotalAmount(1500.0);
        order.setStatusHistory(new ArrayList<>());
    }

    // ---------------------------------------------------------
    // 1. Successful payment
    // ---------------------------------------------------------

    @Test
    void makePayment_shouldCreatePaymentSuccessfully() {

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(paymentRepository.findByOrderId(1L))
                .thenReturn(Optional.empty());

        Payment savedPayment = new Payment();

        savedPayment.setId(10L);
        savedPayment.setOrder(order);
        savedPayment.setAmount(1500.0);
        savedPayment.setPaymentMethod(PaymentMethod.UPI);
        savedPayment.setStatus(PaymentStatus.SUCCESS);
        savedPayment.setTransactionId("TXN123");
        savedPayment.setPaymentDate(
                java.time.LocalDateTime.now()
        );

        when(paymentRepository.save(any(Payment.class)))
                .thenReturn(savedPayment);

        PaymentResponse response =
                paymentService.makePayment(
                        1L,
                        PaymentMethod.UPI,
                        "user@gmail.com"
                );

        assertNotNull(response);

        assertEquals(10L, response.getPaymentId());
        assertEquals(1L, response.getOrderId());
        assertEquals(1500.0, response.getAmount());
        assertEquals(PaymentMethod.UPI, response.getPaymentMethod());
        assertEquals(PaymentStatus.SUCCESS, response.getStatus());
        assertEquals("TXN123", response.getTransactionId());

        verify(paymentRepository).save(any(Payment.class));
        verify(orderRepository).save(order);
    }

    // ---------------------------------------------------------
    // 2. Order not found
    // ---------------------------------------------------------

    @Test
    void makePayment_shouldThrowExceptionWhenOrderNotFound() {

        when(orderRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> paymentService.makePayment(
                        1L,
                        PaymentMethod.UPI,
                        "user@gmail.com"
                )
        );

        verify(paymentRepository, never())
                .save(any(Payment.class));
    }

    // ---------------------------------------------------------
    // 3. Wrong customer
    // ---------------------------------------------------------

    @Test
    void makePayment_shouldRejectWrongCustomer() {

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        assertThrows(
                AccessDeniedException.class,
                () -> paymentService.makePayment(
                        1L,
                        PaymentMethod.UPI,
                        "another@gmail.com"
                )
        );

        verify(paymentRepository, never())
                .save(any(Payment.class));
    }

    // ---------------------------------------------------------
    // 4. Cancelled order
    // ---------------------------------------------------------

    @Test
    void makePayment_shouldRejectCancelledOrder() {

        order.setStatus(OrderStatus.CANCELLED);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.makePayment(
                        1L,
                        PaymentMethod.UPI,
                        "user@gmail.com"
                )
        );

        verify(paymentRepository, never())
                .save(any(Payment.class));
    }

    // ---------------------------------------------------------
    // 5. Confirmed order should be rejected
    // ---------------------------------------------------------

    @Test
    void makePayment_shouldRejectConfirmedOrder() {

        order.setStatus(OrderStatus.CONFIRMED);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.makePayment(
                        1L,
                        PaymentMethod.UPI,
                        "user@gmail.com"
                )
        );

        verify(paymentRepository, never())
                .save(any(Payment.class));
    }

    // ---------------------------------------------------------
    // 6. Shipped order should be rejected
    // ---------------------------------------------------------

    @Test
    void makePayment_shouldRejectShippedOrder() {

        order.setStatus(OrderStatus.SHIPPED);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.makePayment(
                        1L,
                        PaymentMethod.UPI,
                        "user@gmail.com"
                )
        );

        verify(paymentRepository, never())
                .save(any(Payment.class));
    }

    // ---------------------------------------------------------
    // 7. Delivered order should be rejected
    // ---------------------------------------------------------

    @Test
    void makePayment_shouldRejectDeliveredOrder() {

        order.setStatus(OrderStatus.DELIVERED);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.makePayment(
                        1L,
                        PaymentMethod.UPI,
                        "user@gmail.com"
                )
        );

        verify(paymentRepository, never())
                .save(any(Payment.class));
    }

    // ---------------------------------------------------------
    // 8. Duplicate payment
    // ---------------------------------------------------------

    @Test
    void makePayment_shouldRejectDuplicatePayment() {

        Payment existingPayment = new Payment();
        existingPayment.setId(20L);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(paymentRepository.findByOrderId(1L))
                .thenReturn(Optional.of(existingPayment));

        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.makePayment(
                        1L,
                        PaymentMethod.UPI,
                        "user@gmail.com"
                )
        );

        verify(paymentRepository, never())
                .save(any(Payment.class));
    }

    // ---------------------------------------------------------
    // 9. Payment should confirm placed order
    // ---------------------------------------------------------

    @Test
    void makePayment_shouldChangeOrderStatusToConfirmed() {

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(paymentRepository.findByOrderId(1L))
                .thenReturn(Optional.empty());

        Payment payment = new Payment();

        payment.setId(10L);
        payment.setOrder(order);
        payment.setAmount(1500.0);
        payment.setPaymentMethod(PaymentMethod.UPI);
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTransactionId("TXN456");
        payment.setPaymentDate(
                java.time.LocalDateTime.now()
        );

        when(paymentRepository.save(any(Payment.class)))
                .thenReturn(payment);

        paymentService.makePayment(
                1L,
                PaymentMethod.UPI,
                "user@gmail.com"
        );

        assertEquals(
                OrderStatus.CONFIRMED,
                order.getStatus()
        );

        verify(orderRepository).save(order);
    }

    // ---------------------------------------------------------
    // 10. Status history should be created
    // ---------------------------------------------------------

    @Test
    void makePayment_shouldCreateStatusHistory() {

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(paymentRepository.findByOrderId(1L))
                .thenReturn(Optional.empty());

        Payment payment = new Payment();

        payment.setId(10L);
        payment.setOrder(order);
        payment.setAmount(1500.0);
        payment.setPaymentMethod(PaymentMethod.UPI);
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTransactionId("TXN789");
        payment.setPaymentDate(
                java.time.LocalDateTime.now()
        );

        when(paymentRepository.save(any(Payment.class)))
                .thenReturn(payment);

        paymentService.makePayment(
                1L,
                PaymentMethod.UPI,
                "user@gmail.com"
        );

        assertEquals(1, order.getStatusHistory().size());

        OrderStatusHistory history =
                order.getStatusHistory().get(0);

        assertEquals(
                OrderStatus.CONFIRMED,
                history.getStatus()
        );

        assertEquals(
                order,
                history.getOrder()
        );

        assertNotNull(history.getChangedAt());
    }

    // ---------------------------------------------------------
    // 11. Correct amount should be copied from order
    // ---------------------------------------------------------

    @Test
    void makePayment_shouldUseOrderTotalAmount() {

        order.setTotalAmount(2750.50);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(paymentRepository.findByOrderId(1L))
                .thenReturn(Optional.empty());

        Payment payment = new Payment();

        payment.setId(10L);
        payment.setOrder(order);
        payment.setAmount(2750.50);
        payment.setPaymentMethod(PaymentMethod.COD);
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTransactionId("TXN999");
        payment.setPaymentDate(
                java.time.LocalDateTime.now()
        );

        when(paymentRepository.save(any(Payment.class)))
                .thenReturn(payment);

        PaymentResponse response =
                paymentService.makePayment(
                        1L,
                        PaymentMethod.COD,
                        "user@gmail.com"
                );

        assertEquals(
                2750.50,
                response.getAmount()
        );
    }

    // ---------------------------------------------------------
    // 12. Transaction ID should be generated
    // ---------------------------------------------------------

    @Test
    void makePayment_shouldGenerateTransactionId() {

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(paymentRepository.findByOrderId(1L))
                .thenReturn(Optional.empty());

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response =
                paymentService.makePayment(
                        1L,
                        PaymentMethod.UPI,
                        "user@gmail.com"
                );

        assertNotNull(response.getTransactionId());
        assertFalse(
                response.getTransactionId().isBlank()
        );
    }

    // ---------------------------------------------------------
    // 13. Payment date should be generated
    // ---------------------------------------------------------

    @Test
    void makePayment_shouldGeneratePaymentDate() {

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(paymentRepository.findByOrderId(1L))
                .thenReturn(Optional.empty());

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response =
                paymentService.makePayment(
                        1L,
                        PaymentMethod.UPI,
                        "user@gmail.com"
                );

        assertNotNull(response.getPaymentDate());
    }

    // ---------------------------------------------------------
    // 14. Get payment by order ID
    // ---------------------------------------------------------

    @Test
    void getPaymentByOrderId_shouldReturnPayment() {

        Payment payment = new Payment();

        payment.setId(10L);
        payment.setOrder(order);
        payment.setAmount(1500.0);
        payment.setPaymentMethod(PaymentMethod.UPI);
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTransactionId("TXN111");
        payment.setPaymentDate(
                java.time.LocalDateTime.now()
        );

        when(paymentRepository.findByOrderId(1L))
                .thenReturn(Optional.of(payment));

        PaymentResponse response =
                paymentService.getPaymentByOrderId(1L);

        assertNotNull(response);

        assertEquals(10L, response.getPaymentId());
        assertEquals(1L, response.getOrderId());
        assertEquals(1500.0, response.getAmount());
        assertEquals(
                PaymentMethod.UPI,
                response.getPaymentMethod()
        );
        assertEquals(
                PaymentStatus.SUCCESS,
                response.getStatus()
        );
    }

    // ---------------------------------------------------------
    // 15. Payment not found
    // ---------------------------------------------------------

    @Test
    void getPaymentByOrderId_shouldThrowExceptionWhenNotFound() {

        when(paymentRepository.findByOrderId(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> paymentService.getPaymentByOrderId(1L)
        );
    }
}