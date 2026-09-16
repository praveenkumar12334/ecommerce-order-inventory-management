package com.example.ecommerce.repository;

import com.example.ecommerce.entity.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderStatusHistoryRepository
        extends JpaRepository<OrderStatusHistory, Long> {
}