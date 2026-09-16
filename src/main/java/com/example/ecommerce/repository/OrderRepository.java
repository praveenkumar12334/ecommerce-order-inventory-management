package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    long countByStatus(OrderStatus status);

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByCustomerEmail(String CustomerEmail);

    @Query("""
    SELECT DISTINCT o
    FROM Order o
    JOIN o.orderItems oi
    WHERE o.customerEmail = :email
    AND oi.product.id = :productId
    AND o.status = :status
""")
    List<Order> findCustomerOrdersContainingProduct(
            @Param("email") String email,
            @Param("productId") Long productId,
            @Param("status") OrderStatus status
    );
}