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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final OrderRepository orderRepository;

    public DeliveryService(
            DeliveryRepository deliveryRepository,
            OrderRepository orderRepository) {

        this.deliveryRepository = deliveryRepository;
        this.orderRepository = orderRepository;
    }

    // ----------------------------------------------------
    // CREATE DELIVERY
    // ----------------------------------------------------

    @Transactional
    public DeliveryResponse createDelivery(
            Long orderId,
            DeliveryRequest request) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Order not found"
                        ));

        if (deliveryRepository.findByOrderId(orderId).isPresent()) {
            throw new IllegalArgumentException(
                    "Delivery already exists for this order"
            );
        }

        Delivery delivery = new Delivery();

        delivery.setOrder(order);

        delivery.setDeliveryAddress(
                request.getDeliveryAddress()
        );

        delivery.setDeliveryPerson(
                request.getDeliveryPerson()
        );

        delivery.setTrackingNumber(
                "TRK-" + UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
        );

        delivery.setStatus(
                DeliveryStatus.NOT_ASSIGNED
        );

        delivery.setEstimatedDeliveryDate(
                LocalDateTime.now().plusDays(5)
        );

        return convertToResponse(
                deliveryRepository.save(delivery)
        );
    }


    // ----------------------------------------------------
    // ASSIGN DELIVERY
    // ----------------------------------------------------

    @Transactional
    public DeliveryResponse assignDelivery(
            Long deliveryId,
            String deliveryPerson) {

        if (deliveryPerson == null
                || deliveryPerson.isBlank()) {

            throw new IllegalArgumentException(
                    "Delivery person cannot be empty"
            );
        }

        Delivery delivery =
                deliveryRepository.findById(deliveryId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Delivery not found with ID: "
                                                + deliveryId
                                ));

        if (delivery.getStatus()
                != DeliveryStatus.NOT_ASSIGNED) {

            throw new IllegalArgumentException(
                    "Delivery can be assigned only when "
                            + "it is not assigned"
            );
        }

        delivery.setDeliveryPerson(deliveryPerson);

        delivery.setStatus(
                DeliveryStatus.ASSIGNED
        );

        return convertToResponse(
                deliveryRepository.save(delivery)
        );
    }


    // ----------------------------------------------------
    // UPDATE DELIVERY STATUS
    // ----------------------------------------------------

    @Transactional
    public DeliveryResponse updateStatus(
            Long deliveryId,
            DeliveryStatus newStatus) {

        Delivery delivery =
                deliveryRepository.findById(deliveryId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Delivery not found"
                                ));

        DeliveryStatus currentStatus =
                delivery.getStatus();

        // ------------------------------------------------
        // Terminal states
        // ------------------------------------------------

        if (currentStatus == DeliveryStatus.DELIVERED) {

            throw new IllegalArgumentException(
                    "Delivered delivery cannot be updated"
            );
        }

        if (currentStatus == DeliveryStatus.FAILED) {

            throw new IllegalArgumentException(
                    "Failed delivery cannot be updated"
            );
        }


        // ------------------------------------------------
        // Validate status transition
        // ------------------------------------------------

        boolean validTransition =

                // NOT_ASSIGNED → ASSIGNED
                (currentStatus == DeliveryStatus.NOT_ASSIGNED
                        && newStatus == DeliveryStatus.ASSIGNED)

                        ||

                        // ASSIGNED → OUT_FOR_DELIVERY
                        // ASSIGNED → FAILED
                        (currentStatus == DeliveryStatus.ASSIGNED
                                && (newStatus
                                == DeliveryStatus.OUT_FOR_DELIVERY
                                || newStatus
                                == DeliveryStatus.FAILED))

                        ||

                        // OUT_FOR_DELIVERY → DELIVERED
                        // OUT_FOR_DELIVERY → FAILED
                        (currentStatus
                                == DeliveryStatus.OUT_FOR_DELIVERY
                                && (newStatus
                                == DeliveryStatus.DELIVERED
                                || newStatus
                                == DeliveryStatus.FAILED));


        if (!validTransition) {

            throw new IllegalArgumentException(
                    "Invalid delivery status transition from "
                            + currentStatus
                            + " to "
                            + newStatus
            );
        }


        // ------------------------------------------------
        // Update delivery status
        // ------------------------------------------------

        delivery.setStatus(newStatus);


        // ------------------------------------------------
        // OUT FOR DELIVERY
        // Order: CONFIRMED → SHIPPED
        // ------------------------------------------------

        if (newStatus
                == DeliveryStatus.OUT_FOR_DELIVERY) {

            Order order = delivery.getOrder();

            if (order.getStatus()
                    == OrderStatus.CONFIRMED) {

                order.setStatus(
                        OrderStatus.SHIPPED
                );

                orderRepository.save(order);
            }
        }


        // ------------------------------------------------
        // DELIVERED
        // Order → DELIVERED
        // ------------------------------------------------

        if (newStatus
                == DeliveryStatus.DELIVERED) {

            delivery.setDeliveredDate(
                    LocalDateTime.now()
            );

            Order order = delivery.getOrder();

            order.setStatus(
                    OrderStatus.DELIVERED
            );

            orderRepository.save(order);
        }


        return convertToResponse(
                deliveryRepository.save(delivery)
        );
    }


    // ----------------------------------------------------
    // GET DELIVERY BY ORDER
    // ----------------------------------------------------

    public DeliveryResponse getDeliveryByOrderId(
            Long orderId) {

        Delivery delivery =
                deliveryRepository.findByOrderId(orderId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Delivery not found for order ID: "
                                                + orderId
                                ));

        return convertToResponse(delivery);
    }


    // ----------------------------------------------------
    // CONVERT ENTITY → RESPONSE
    // ----------------------------------------------------

    private DeliveryResponse convertToResponse(
            Delivery delivery) {

        DeliveryResponse response =
                new DeliveryResponse();

        response.setDeliveryId(
                delivery.getId()
        );

        response.setOrderId(
                delivery.getOrder().getId()
        );

        response.setDeliveryAddress(
                delivery.getDeliveryAddress()
        );

        response.setDeliveryPerson(
                delivery.getDeliveryPerson()
        );

        response.setTrackingNumber(
                delivery.getTrackingNumber()
        );

        response.setStatus(
                delivery.getStatus()
        );

        response.setEstimatedDeliveryDate(
                delivery.getEstimatedDeliveryDate()
        );

        response.setDeliveredDate(
                delivery.getDeliveredDate()
        );

        return response;
    }
}