package com.example.ecommerce.service;

import com.example.ecommerce.dto.DashboardResponse;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.OrderStatus;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public DashboardService(UserRepository userRepository,
                            ProductRepository productRepository,
                            OrderRepository orderRepository) {

        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    public DashboardResponse getDashboard() {

        DashboardResponse response = new DashboardResponse();

        response.setTotalUsers(userRepository.count());

        response.setTotalProducts(productRepository.count());

        response.setTotalOrders(orderRepository.count());

        response.setPendingOrders(
                orderRepository.countByStatus(OrderStatus.PLACED)
        );

        response.setConfirmedOrders(
                orderRepository.countByStatus(OrderStatus.CONFIRMED)
        );

        response.setShippedOrders(
                orderRepository.countByStatus(OrderStatus.SHIPPED)
        );

        response.setDeliveredOrders(
                orderRepository.countByStatus(OrderStatus.DELIVERED)
        );

        response.setCancelledOrders(
                orderRepository.countByStatus(OrderStatus.CANCELLED)
        );

        double revenue = calculateRevenue();

        response.setTotalRevenue(revenue);

        return response;
    }

    private double calculateRevenue() {

        List<Order> orders = orderRepository.findAll();

        double revenue = 0;

        for (Order order : orders) {

            if (order.getStatus() != OrderStatus.CANCELLED) {
                revenue += order.getTotalAmount();
            }
        }

        return revenue;
    }
}