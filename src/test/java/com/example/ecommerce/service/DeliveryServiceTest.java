package com.example.ecommerce.service;

import com.example.ecommerce.dto.DeliveryRequest;
import com.example.ecommerce.dto.DeliveryResponse;
import com.example.ecommerce.entity.Delivery;
import com.example.ecommerce.entity.DeliveryStatus;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.OrderStatus;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.DeliveryRepository;
import com.example.ecommerce.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

    @Mock
    private DeliveryRepository deliveryRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private DeliveryService deliveryService;

    private Order order;
    private Delivery delivery;
    private DeliveryRequest request;

    @BeforeEach
    void setUp() {

        order = new Order();
        order.setId(1L);
        order.setCustomerName("Test User");
        order.setCustomerEmail("user@gmail.com");
        order.setStatus(OrderStatus.CONFIRMED);

        delivery = new Delivery();
        delivery.setId(10L);
        delivery.setOrder(order);
        delivery.setDeliveryAddress("123 Main Street");
        delivery.setDeliveryPerson(null);
        delivery.setTrackingNumber("TRK-12345678");
        delivery.setStatus(DeliveryStatus.NOT_ASSIGNED);
        delivery.setEstimatedDeliveryDate(
                LocalDateTime.now().plusDays(5)
        );

        request = new DeliveryRequest();
        request.setDeliveryAddress("123 Main Street");
        request.setDeliveryPerson(null);
    }

    // =========================================================
    // createDelivery()
    // =========================================================

    @Test
    void createDelivery_shouldCreateSuccessfully() {

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(deliveryRepository.findByOrderId(1L))
                .thenReturn(Optional.empty());

        when(deliveryRepository.save(any(Delivery.class)))
                .thenAnswer(invocation -> {
                    Delivery saved =
                            invocation.getArgument(0);

                    saved.setId(10L);

                    return saved;
                });

        DeliveryResponse response =
                deliveryService.createDelivery(
                        1L,
                        request
                );

        assertNotNull(response);
        assertEquals(10L, response.getDeliveryId());
        assertEquals(1L, response.getOrderId());
        assertEquals(
                "123 Main Street",
                response.getDeliveryAddress()
        );

        assertEquals(
                DeliveryStatus.NOT_ASSIGNED,
                response.getStatus()
        );

        assertNotNull(
                response.getTrackingNumber()
        );

        assertTrue(
                response.getTrackingNumber()
                        .startsWith("TRK-")
        );

        assertNotNull(
                response.getEstimatedDeliveryDate()
        );

        verify(deliveryRepository)
                .save(any(Delivery.class));
    }

    @Test
    void createDelivery_shouldThrowWhenOrderNotFound() {

        when(orderRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> deliveryService.createDelivery(
                        1L,
                        request
                )
        );

        verify(deliveryRepository, never())
                .save(any(Delivery.class));
    }

    @Test
    void createDelivery_shouldRejectDuplicateDelivery() {

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(deliveryRepository.findByOrderId(1L))
                .thenReturn(Optional.of(delivery));

        assertThrows(
                IllegalArgumentException.class,
                () -> deliveryService.createDelivery(
                        1L,
                        request
                )
        );

        verify(deliveryRepository, never())
                .save(any(Delivery.class));
    }

    @Test
    void createDelivery_shouldGenerateTrackingNumber() {

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(deliveryRepository.findByOrderId(1L))
                .thenReturn(Optional.empty());

        when(deliveryRepository.save(any(Delivery.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        DeliveryResponse response =
                deliveryService.createDelivery(
                        1L,
                        request
                );

        assertNotNull(
                response.getTrackingNumber()
        );

        assertTrue(
                response.getTrackingNumber()
                        .startsWith("TRK-")
        );

        assertEquals(
                12,
                response.getTrackingNumber().length()
        );
    }

    // =========================================================
    // assignDelivery()
    // =========================================================

    @Test
    void assignDelivery_shouldAssignSuccessfully() {

        when(deliveryRepository.findById(10L))
                .thenReturn(Optional.of(delivery));

        when(deliveryRepository.save(delivery))
                .thenReturn(delivery);

        DeliveryResponse response =
                deliveryService.assignDelivery(
                        10L,
                        "Ravi"
                );

        assertEquals(
                "Ravi",
                response.getDeliveryPerson()
        );

        assertEquals(
                DeliveryStatus.ASSIGNED,
                response.getStatus()
        );

        verify(deliveryRepository)
                .save(delivery);
    }

    @Test
    void assignDelivery_shouldRejectBlankPerson() {

        assertThrows(
                IllegalArgumentException.class,
                () -> deliveryService.assignDelivery(
                        10L,
                        ""
                )
        );

        verify(deliveryRepository, never())
                .save(any(Delivery.class));
    }

    @Test
    void assignDelivery_shouldRejectNullPerson() {

        assertThrows(
                IllegalArgumentException.class,
                () -> deliveryService.assignDelivery(
                        10L,
                        null
                )
        );

        verify(deliveryRepository, never())
                .save(any(Delivery.class));
    }

    @Test
    void assignDelivery_shouldRejectAlreadyAssignedDelivery() {

        delivery.setStatus(
                DeliveryStatus.ASSIGNED
        );

        when(deliveryRepository.findById(10L))
                .thenReturn(Optional.of(delivery));

        assertThrows(
                IllegalArgumentException.class,
                () -> deliveryService.assignDelivery(
                        10L,
                        "Ravi"
                )
        );

        verify(deliveryRepository, never())
                .save(any(Delivery.class));
    }

    @Test
    void assignDelivery_shouldThrowWhenNotFound() {

        when(deliveryRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> deliveryService.assignDelivery(
                        10L,
                        "Ravi"
                )
        );
    }

    // =========================================================
    // updateStatus()
    // =========================================================

    @Test
    void updateStatus_shouldChangeAssignedToOutForDelivery() {

        delivery.setStatus(
                DeliveryStatus.ASSIGNED
        );

        when(deliveryRepository.findById(10L))
                .thenReturn(Optional.of(delivery));

        when(deliveryRepository.save(delivery))
                .thenReturn(delivery);

        DeliveryResponse response =
                deliveryService.updateStatus(
                        10L,
                        DeliveryStatus.OUT_FOR_DELIVERY
                );

        assertEquals(
                DeliveryStatus.OUT_FOR_DELIVERY,
                response.getStatus()
        );

        assertEquals(
                OrderStatus.SHIPPED,
                order.getStatus()
        );

        verify(orderRepository)
                .save(order);

        verify(deliveryRepository)
                .save(delivery);
    }

    @Test
    void updateStatus_shouldChangeOutForDeliveryToDelivered() {

        delivery.setStatus(
                DeliveryStatus.OUT_FOR_DELIVERY
        );

        order.setStatus(
                OrderStatus.SHIPPED
        );

        when(deliveryRepository.findById(10L))
                .thenReturn(Optional.of(delivery));

        when(deliveryRepository.save(delivery))
                .thenReturn(delivery);

        DeliveryResponse response =
                deliveryService.updateStatus(
                        10L,
                        DeliveryStatus.DELIVERED
                );

        assertEquals(
                DeliveryStatus.DELIVERED,
                response.getStatus()
        );

        assertEquals(
                OrderStatus.DELIVERED,
                order.getStatus()
        );

        assertNotNull(
                response.getDeliveredDate()
        );

        verify(orderRepository)
                .save(order);

        verify(deliveryRepository)
                .save(delivery);
    }

    @Test
    void updateStatus_shouldAllowAssignedToFailed() {

        delivery.setStatus(
                DeliveryStatus.ASSIGNED
        );

        when(deliveryRepository.findById(10L))
                .thenReturn(Optional.of(delivery));

        when(deliveryRepository.save(delivery))
                .thenReturn(delivery);

        DeliveryResponse response =
                deliveryService.updateStatus(
                        10L,
                        DeliveryStatus.FAILED
                );

        assertEquals(
                DeliveryStatus.FAILED,
                response.getStatus()
        );

        verify(deliveryRepository)
                .save(delivery);

        verify(orderRepository, never())
                .save(any(Order.class));
    }

    @Test
    void updateStatus_shouldAllowOutForDeliveryToFailed() {

        delivery.setStatus(
                DeliveryStatus.OUT_FOR_DELIVERY
        );

        when(deliveryRepository.findById(10L))
                .thenReturn(Optional.of(delivery));

        when(deliveryRepository.save(delivery))
                .thenReturn(delivery);

        DeliveryResponse response =
                deliveryService.updateStatus(
                        10L,
                        DeliveryStatus.FAILED
                );

        assertEquals(
                DeliveryStatus.FAILED,
                response.getStatus()
        );
    }

    // =========================================================
    // Invalid transitions
    // =========================================================

    @Test
    void updateStatus_shouldRejectNotAssignedToDelivered() {

        when(deliveryRepository.findById(10L))
                .thenReturn(Optional.of(delivery));

        assertThrows(
                IllegalArgumentException.class,
                () -> deliveryService.updateStatus(
                        10L,
                        DeliveryStatus.DELIVERED
                )
        );

        verify(deliveryRepository, never())
                .save(any(Delivery.class));
    }

    @Test
    void updateStatus_shouldRejectNotAssignedToOutForDelivery() {

        when(deliveryRepository.findById(10L))
                .thenReturn(Optional.of(delivery));

        assertThrows(
                IllegalArgumentException.class,
                () -> deliveryService.updateStatus(
                        10L,
                        DeliveryStatus.OUT_FOR_DELIVERY
                )
        );

        verify(deliveryRepository, never())
                .save(any(Delivery.class));
    }

    @Test
    void updateStatus_shouldRejectAssignedToDelivered() {

        delivery.setStatus(
                DeliveryStatus.ASSIGNED
        );

        when(deliveryRepository.findById(10L))
                .thenReturn(Optional.of(delivery));

        assertThrows(
                IllegalArgumentException.class,
                () -> deliveryService.updateStatus(
                        10L,
                        DeliveryStatus.DELIVERED
                )
        );

        verify(deliveryRepository, never())
                .save(any(Delivery.class));
    }

    @Test
    void updateStatus_shouldRejectOutForDeliveryToAssigned() {

        delivery.setStatus(
                DeliveryStatus.OUT_FOR_DELIVERY
        );

        when(deliveryRepository.findById(10L))
                .thenReturn(Optional.of(delivery));

        assertThrows(
                IllegalArgumentException.class,
                () -> deliveryService.updateStatus(
                        10L,
                        DeliveryStatus.ASSIGNED
                )
        );

        verify(deliveryRepository, never())
                .save(any(Delivery.class));
    }

    @Test
    void updateStatus_shouldRejectDeliveredAsTerminal() {

        delivery.setStatus(
                DeliveryStatus.DELIVERED
        );

        when(deliveryRepository.findById(10L))
                .thenReturn(Optional.of(delivery));

        assertThrows(
                IllegalArgumentException.class,
                () -> deliveryService.updateStatus(
                        10L,
                        DeliveryStatus.ASSIGNED
                )
        );

        verify(deliveryRepository, never())
                .save(any(Delivery.class));
    }

    @Test
    void updateStatus_shouldRejectFailedAsTerminal() {

        delivery.setStatus(
                DeliveryStatus.FAILED
        );

        when(deliveryRepository.findById(10L))
                .thenReturn(Optional.of(delivery));

        assertThrows(
                IllegalArgumentException.class,
                () -> deliveryService.updateStatus(
                        10L,
                        DeliveryStatus.ASSIGNED
                )
        );

        verify(deliveryRepository, never())
                .save(any(Delivery.class));
    }

    @Test
    void updateStatus_shouldThrowWhenDeliveryNotFound() {

        when(deliveryRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> deliveryService.updateStatus(
                        10L,
                        DeliveryStatus.ASSIGNED
                )
        );
    }

    // =========================================================
    // getDeliveryByOrderId()
    // =========================================================

    @Test
    void getDeliveryByOrderId_shouldReturnDelivery() {

        when(deliveryRepository.findByOrderId(1L))
                .thenReturn(Optional.of(delivery));

        DeliveryResponse response =
                deliveryService.getDeliveryByOrderId(1L);

        assertNotNull(response);

        assertEquals(
                10L,
                response.getDeliveryId()
        );

        assertEquals(
                1L,
                response.getOrderId()
        );

        assertEquals(
                DeliveryStatus.NOT_ASSIGNED,
                response.getStatus()
        );

        assertEquals(
                "TRK-12345678",
                response.getTrackingNumber()
        );
    }

    @Test
    void getDeliveryByOrderId_shouldThrowWhenNotFound() {

        when(deliveryRepository.findByOrderId(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> deliveryService
                        .getDeliveryByOrderId(1L)
        );
    }

    // =========================================================
    // Additional order-status behavior
    // =========================================================

    @Test
    void updateStatus_shouldNotChangeOrderWhenAlreadyShipped() {

        delivery.setStatus(
                DeliveryStatus.ASSIGNED
        );

        order.setStatus(
                OrderStatus.SHIPPED
        );

        when(deliveryRepository.findById(10L))
                .thenReturn(Optional.of(delivery));

        when(deliveryRepository.save(delivery))
                .thenReturn(delivery);

        deliveryService.updateStatus(
                10L,
                DeliveryStatus.OUT_FOR_DELIVERY
        );

        assertEquals(
                OrderStatus.SHIPPED,
                order.getStatus()
        );

        verify(orderRepository, never())
                .save(any(Order.class));
    }

    @Test
    void updateStatus_shouldSetDeliveredDateOnlyWhenDelivered() {

        delivery.setStatus(
                DeliveryStatus.OUT_FOR_DELIVERY
        );

        when(deliveryRepository.findById(10L))
                .thenReturn(Optional.of(delivery));

        when(deliveryRepository.save(delivery))
                .thenReturn(delivery);

        deliveryService.updateStatus(
                10L,
                DeliveryStatus.FAILED
        );

        assertNull(
                delivery.getDeliveredDate()
        );
    }
}